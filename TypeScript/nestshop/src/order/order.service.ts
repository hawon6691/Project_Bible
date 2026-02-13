import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, DataSource, In, EntityManager } from 'typeorm';
import { Order, OrderStatus } from './entities/order.entity';
import { OrderItem } from './entities/order-item.entity';
import { Payment, PaymentStatus } from './entities/payment.entity';
import { Product } from '../product/entities/product.entity';
import { Seller } from '../seller/entities/seller.entity';
import { User } from '../user/entities/user.entity';
import { CartItem } from '../cart/entities/cart-item.entity';
import { Address } from '../address/entities/address.entity';
import { CreateOrderDto } from './dto/create-order.dto';
import { CreatePaymentDto, RefundPaymentDto } from './dto/payment.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';

@Injectable()
export class OrderService {
  constructor(
    @InjectRepository(Order)
    private orderRepository: Repository<Order>,
    @InjectRepository(OrderItem)
    private orderItemRepository: Repository<OrderItem>,
    @InjectRepository(Payment)
    private paymentRepository: Repository<Payment>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(Seller)
    private sellerRepository: Repository<Seller>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
    private dataSource: DataSource,
  ) {}

  // ORD-01: 주문 생성
  // 재고 차감/포인트 사용/주문 생성을 하나의 트랜잭션으로 묶어 정합성을 보장한다.
  async create(userId: number, dto: CreateOrderDto) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction('SERIALIZABLE');

    try {
      // 주문 시점의 배송지 스냅샷을 남기기 위해 배송지 엔티티를 조회한다.
      const address = await queryRunner.manager.findOne(Address, {
        where: { id: dto.addressId, userId },
      });
      if (!address) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      const user = await queryRunner.manager.findOne(User, { where: { id: userId } });
      if (!user) {
        throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      const usePoint = dto.usePoint || 0;
      if (usePoint > user.point) {
        throw new BusinessException('POINT_INSUFFICIENT', HttpStatus.BAD_REQUEST);
      }

      let totalAmount = 0;
      const orderItems: Partial<OrderItem>[] = [];

      // 주문 요청 항목을 순회하며 존재/재고/판매처를 검증하고 총액을 계산한다.
      for (const item of dto.items) {
        const product = await queryRunner.manager.findOne(Product, {
          where: { id: item.productId },
        });
        if (!product) {
          throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
        }
        if (product.stock < item.quantity) {
          throw new BusinessException('PRODUCT_OUT_OF_STOCK', HttpStatus.BAD_REQUEST);
        }

        const seller = await queryRunner.manager.findOne(Seller, {
          where: { id: item.sellerId },
        });
        if (!seller) {
          throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
        }

        const unitPrice = product.discountPrice || product.price;
        const itemTotal = unitPrice * item.quantity;
        totalAmount += itemTotal;

        // 주문 생성 시 즉시 재고/판매량 반영
        product.stock -= item.quantity;
        product.salesCount += item.quantity;
        await queryRunner.manager.save(product);

        orderItems.push({
          productId: item.productId,
          sellerId: item.sellerId,
          productName: product.name,
          sellerName: seller.name,
          selectedOptions: item.selectedOptions || null,
          quantity: item.quantity,
          unitPrice,
          totalPrice: itemTotal,
        });
      }

      const finalAmount = totalAmount - usePoint;
      if (finalAmount < 0) {
        throw new BusinessException('PAYMENT_FAILED', HttpStatus.BAD_REQUEST, '사용 포인트가 주문 금액을 초과했습니다.');
      }

      const orderNumber = this.generateOrderNumber();

      // 결제 금액이 0원인 주문은 결제 완료로 바로 처리한다.
      const order = queryRunner.manager.create(Order, {
        orderNumber,
        userId,
        status: finalAmount === 0 ? OrderStatus.PAYMENT_CONFIRMED : OrderStatus.ORDER_PLACED,
        totalAmount,
        pointUsed: usePoint,
        finalAmount,
        recipientName: address.recipientName,
        recipientPhone: address.phone,
        zipCode: address.zipCode,
        address: address.address,
        addressDetail: address.addressDetail,
        memo: dto.memo || null,
      });

      const savedOrder = await queryRunner.manager.save(order);

      for (const item of orderItems) {
        const orderItem = queryRunner.manager.create(OrderItem, {
          ...item,
          orderId: savedOrder.id,
        });
        await queryRunner.manager.save(orderItem);
      }

      if (usePoint > 0) {
        user.point -= usePoint;
        await queryRunner.manager.save(user);
      }

      if (dto.fromCart && dto.cartItemIds?.length) {
        await queryRunner.manager.delete(CartItem, {
          id: In(dto.cartItemIds),
          userId,
        });
      }

      await queryRunner.commitTransaction();
      return this.findOne(userId, savedOrder.id);
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  // PAY-01: 결제 요청
  // ORDER_PLACED -> PAYMENT_PENDING -> PAYMENT_CONFIRMED 전이를 강제한다.
  async requestPayment(userId: number, dto: CreatePaymentDto) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const order = await queryRunner.manager.findOne(Order, {
        where: { id: dto.orderId, userId },
        relations: ['payments'],
      });
      if (!order) {
        throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      if (order.finalAmount !== dto.amount) {
        throw new BusinessException('PAYMENT_FAILED', HttpStatus.BAD_REQUEST, '결제 금액이 주문 금액과 일치하지 않습니다.');
      }

      if (![OrderStatus.ORDER_PLACED, OrderStatus.PAYMENT_PENDING].includes(order.status)) {
        throw new BusinessException('ORDER_CANNOT_CANCEL', HttpStatus.BAD_REQUEST, '결제를 진행할 수 없는 주문 상태입니다.');
      }

      const hasCompletedPayment = (order.payments || []).some((payment) => payment.status === PaymentStatus.COMPLETED);
      if (hasCompletedPayment) {
        throw new BusinessException('PAYMENT_FAILED', HttpStatus.BAD_REQUEST, '이미 결제 완료된 주문입니다.');
      }

      if (order.status === OrderStatus.ORDER_PLACED) {
        this.assertStatusTransition(order.status, OrderStatus.PAYMENT_PENDING);
        order.status = OrderStatus.PAYMENT_PENDING;
        await queryRunner.manager.save(order);
      }

      // 이전에 실패하지 못하고 남아 있는 PENDING 결제는 모두 실패 처리한다.
      const pendingPayments = (order.payments || []).filter((payment) => payment.status === PaymentStatus.PENDING);
      for (const pendingPayment of pendingPayments) {
        pendingPayment.status = PaymentStatus.FAILED;
        await queryRunner.manager.save(pendingPayment);
      }

      // 모의 결제 구현: 생성 직후 완료로 바꿔 상태 전이를 명확히 검증한다.
      const payment = queryRunner.manager.create(Payment, {
        orderId: order.id,
        method: dto.method,
        amount: dto.amount,
        status: PaymentStatus.PENDING,
        paidAt: null,
        refundedAt: null,
      });
      const savedPayment = await queryRunner.manager.save(payment);

      savedPayment.status = PaymentStatus.COMPLETED;
      savedPayment.paidAt = new Date();
      const completedPayment = await queryRunner.manager.save(savedPayment);

      this.assertStatusTransition(order.status, OrderStatus.PAYMENT_CONFIRMED);
      order.status = OrderStatus.PAYMENT_CONFIRMED;
      await queryRunner.manager.save(order);

      await queryRunner.commitTransaction();
      return this.toPaymentDetail(completedPayment, order);
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  // PAY-02: 결제 조회
  async getPayment(userId: number, paymentId: number) {
    const payment = await this.paymentRepository.findOne({
      where: { id: paymentId },
      relations: ['order'],
    });

    if (!payment || payment.order.userId !== userId) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return this.toPaymentDetail(payment, payment.order);
  }

  // PAY-03: 환불 처리
  // CANCELLED 또는 RETURN_REQUESTED 상태에서만 환불을 허용한다.
  async refundPayment(userId: number, paymentId: number, dto: RefundPaymentDto, isAdmin = false) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const payment = await queryRunner.manager.findOne(Payment, {
        where: { id: paymentId },
        relations: ['order', 'order.items'],
      });
      if (!payment) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      if (!isAdmin && payment.order.userId !== userId) {
        throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
      }

      if (payment.status === PaymentStatus.REFUNDED) {
        throw new BusinessException('PAYMENT_ALREADY_REFUNDED', HttpStatus.BAD_REQUEST);
      }

      if (payment.status !== PaymentStatus.COMPLETED) {
        throw new BusinessException('PAYMENT_FAILED', HttpStatus.BAD_REQUEST, '환불 가능한 결제 상태가 아닙니다.');
      }

      if (![OrderStatus.CANCELLED, OrderStatus.RETURN_REQUESTED].includes(payment.order.status)) {
        throw new BusinessException('ORDER_CANNOT_CANCEL', HttpStatus.BAD_REQUEST, '주문 취소 또는 반품 요청 상태에서만 환불할 수 있습니다.');
      }

      payment.status = PaymentStatus.REFUNDED;
      payment.refundedAt = new Date();
      const refundedPayment = await queryRunner.manager.save(payment);

      // 반품 요청 주문은 환불 완료 시 RETURNED로 마무리한다.
      if (payment.order.status === OrderStatus.RETURN_REQUESTED) {
        await this.restoreOrderInventory(queryRunner.manager, payment.order.items || []);
        await this.refundUsedPoints(queryRunner.manager, payment.order.userId, payment.order.pointUsed);
        this.assertStatusTransition(payment.order.status, OrderStatus.RETURNED);
        payment.order.status = OrderStatus.RETURNED;
        await queryRunner.manager.save(payment.order);
      }

      await queryRunner.commitTransaction();
      return {
        ...this.toPaymentDetail(refundedPayment, payment.order),
        reason: dto.reason || null,
      };
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  // ORD-02: 내 주문 목록
  async findMyOrders(userId: number, query: PaginationRequestDto) {
    const [items, totalItems] = await this.orderRepository.findAndCount({
      where: { userId },
      relations: ['items'],
      skip: query.skip,
      take: query.limit,
      order: { createdAt: 'DESC' },
    });

    return new PaginationResponseDto(
      items.map((o) => this.toSummary(o)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // ORD-03: 주문 상세 조회
  async findOne(userId: number, orderId: number) {
    const order = await this.orderRepository.findOne({
      where: { id: orderId, userId },
      relations: ['items', 'payments'],
    });
    if (!order) {
      throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toDetail(order);
  }

  // ORD-04 확장: 반품 요청
  // DELIVERED/CONFIRMED 상태에서만 반품 요청을 허용한다.
  async requestReturn(userId: number, orderId: number) {
    const order = await this.orderRepository.findOne({
      where: { id: orderId, userId },
      relations: ['items', 'payments'],
    });
    if (!order) {
      throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (![OrderStatus.DELIVERED, OrderStatus.CONFIRMED].includes(order.status)) {
      throw new BusinessException(
        'ORDER_CANNOT_CANCEL',
        HttpStatus.BAD_REQUEST,
        '배송 완료 또는 구매 확정 상태에서만 반품 요청할 수 있습니다.',
      );
    }

    this.assertStatusTransition(order.status, OrderStatus.RETURN_REQUESTED);
    order.status = OrderStatus.RETURN_REQUESTED;
    await this.orderRepository.save(order);

    return this.findOne(userId, order.id);
  }

  // ORD-04: 주문 취소
  // 결제 완료 이력이 있으면 직접 취소를 막고 환불 경로를 강제한다.
  async cancel(userId: number, orderId: number) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const order = await queryRunner.manager.findOne(Order, {
        where: { id: orderId, userId },
        relations: ['items', 'payments'],
      });
      if (!order) {
        throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      if (order.status === OrderStatus.CANCELLED) {
        throw new BusinessException('ORDER_ALREADY_CANCELLED', HttpStatus.BAD_REQUEST);
      }

      this.assertStatusTransition(order.status, OrderStatus.CANCELLED);

      const hasCompletedPayment = (order.payments || []).some(
        (payment) => payment.status === PaymentStatus.COMPLETED,
      );
      if (hasCompletedPayment) {
        throw new BusinessException('ORDER_CANNOT_CANCEL', HttpStatus.BAD_REQUEST, '결제 완료된 주문은 먼저 환불 처리가 필요합니다.');
      }

      await this.restoreOrderInventory(queryRunner.manager, order.items);
      await this.refundUsedPoints(queryRunner.manager, userId, order.pointUsed);

      order.status = OrderStatus.CANCELLED;
      await queryRunner.manager.save(order);

      await queryRunner.commitTransaction();
      return this.findOne(userId, order.id);
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  // ORD-05: 전체 주문 관리 (Admin)
  async findAllOrders(query: PaginationRequestDto) {
    const [items, totalItems] = await this.orderRepository.findAndCount({
      relations: ['items'],
      skip: query.skip,
      take: query.limit,
      order: { createdAt: 'DESC' },
    });

    return new PaginationResponseDto(
      items.map((o) => this.toSummary(o)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // ORD-06: 주문 상태 변경 (Admin)
  // Admin도 상태머신 규칙을 우회하지 못하게 동일 전이 검증을 적용한다.
  async updateStatus(orderId: number, status: OrderStatus) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const order = await queryRunner.manager.findOne(Order, {
        where: { id: orderId },
        relations: ['items', 'payments'],
      });
      if (!order) {
        throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      if (order.status === status) {
        await queryRunner.commitTransaction();
        return this.toDetail(order);
      }

      this.assertStatusTransition(order.status, status);

      const hasCompletedPayment = (order.payments || []).some(
        (payment) => payment.status === PaymentStatus.COMPLETED,
      );
      const hasRefundedPayment = (order.payments || []).some(
        (payment) => payment.status === PaymentStatus.REFUNDED,
      );

      if (status === OrderStatus.PAYMENT_CONFIRMED && !hasCompletedPayment) {
        throw new BusinessException('PAYMENT_FAILED', HttpStatus.BAD_REQUEST, '결제 완료 내역이 없어 PAYMENT_CONFIRMED로 변경할 수 없습니다.');
      }

      if (status === OrderStatus.CANCELLED && hasCompletedPayment && !hasRefundedPayment) {
        throw new BusinessException('ORDER_CANNOT_CANCEL', HttpStatus.BAD_REQUEST, '결제 완료 주문은 환불 처리 후 취소 상태로 변경할 수 있습니다.');
      }

      if (status === OrderStatus.RETURNED && hasCompletedPayment && !hasRefundedPayment) {
        throw new BusinessException('PAYMENT_FAILED', HttpStatus.BAD_REQUEST, '환불 처리 후 RETURNED 상태로 변경할 수 있습니다.');
      }

      // Admin 경로에서도 도메인 정합성을 맞추기 위해 롤백 처리 로직을 동일 적용한다.
      if (status === OrderStatus.CANCELLED && [OrderStatus.ORDER_PLACED, OrderStatus.PAYMENT_PENDING].includes(order.status)) {
        await this.restoreOrderInventory(queryRunner.manager, order.items || []);
        await this.refundUsedPoints(queryRunner.manager, order.userId, order.pointUsed);
      }

      if (status === OrderStatus.RETURNED && order.status === OrderStatus.RETURN_REQUESTED) {
        await this.restoreOrderInventory(queryRunner.manager, order.items || []);
        await this.refundUsedPoints(queryRunner.manager, order.userId, order.pointUsed);
      }

      order.status = status;
      const saved = await queryRunner.manager.save(order);

      await queryRunner.commitTransaction();
      return this.toDetail(saved);
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  // 주문 상태머신 규칙
  // 잘못된 순서 전이를 한 곳에서 막기 위해 공통으로 사용한다.
  private assertStatusTransition(current: OrderStatus, target: OrderStatus) {
    if (current === target) {
      return;
    }

    const allowedTransitions: Record<OrderStatus, OrderStatus[]> = {
      [OrderStatus.ORDER_PLACED]: [OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED],
      [OrderStatus.PAYMENT_PENDING]: [OrderStatus.PAYMENT_CONFIRMED, OrderStatus.CANCELLED],
      [OrderStatus.PAYMENT_CONFIRMED]: [OrderStatus.PREPARING],
      [OrderStatus.PREPARING]: [OrderStatus.SHIPPING],
      [OrderStatus.SHIPPING]: [OrderStatus.DELIVERED],
      [OrderStatus.DELIVERED]: [OrderStatus.CONFIRMED, OrderStatus.RETURN_REQUESTED],
      [OrderStatus.CONFIRMED]: [OrderStatus.RETURN_REQUESTED],
      [OrderStatus.CANCELLED]: [],
      [OrderStatus.RETURN_REQUESTED]: [OrderStatus.RETURNED],
      [OrderStatus.RETURNED]: [],
    };

    if (!allowedTransitions[current].includes(target)) {
      throw new BusinessException(
        'ORDER_CANNOT_CANCEL',
        HttpStatus.BAD_REQUEST,
        `허용되지 않은 주문 상태 변경입니다. (${current} -> ${target})`,
      );
    }
  }

  // 주문번호 생성
  private generateOrderNumber(): string {
    const now = new Date();
    const date = now.toISOString().slice(0, 10).replace(/-/g, '');
    const random = Math.random().toString(36).substring(2, 8).toUpperCase();
    return `ORD-${date}-${random}`;
  }

  // 목록 응답 요약 형태
  private toSummary(order: Order) {
    return {
      id: order.id,
      orderNumber: order.orderNumber,
      status: order.status,
      totalAmount: order.totalAmount,
      finalAmount: order.finalAmount,
      itemCount: order.items?.length || 0,
      createdAt: order.createdAt,
    };
  }

  // 결제 응답 형태 통일
  private toPaymentDetail(payment: Payment, order: Order) {
    return {
      id: payment.id,
      orderId: payment.orderId,
      orderStatus: order.status,
      method: payment.method,
      amount: payment.amount,
      status: payment.status,
      paidAt: payment.paidAt,
      refundedAt: payment.refundedAt,
      createdAt: payment.createdAt,
      updatedAt: payment.updatedAt,
    };
  }

  // 주문 상세 응답 형태
  private toDetail(order: Order) {
    return {
      id: order.id,
      orderNumber: order.orderNumber,
      status: order.status,
      items: (order.items || []).map((item) => ({
        id: item.id,
        productId: item.productId,
        productName: item.productName,
        sellerName: item.sellerName,
        selectedOptions: item.selectedOptions,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        totalPrice: item.totalPrice,
      })),
      totalAmount: order.totalAmount,
      pointUsed: order.pointUsed,
      finalAmount: order.finalAmount,
      shippingAddress: {
        recipientName: order.recipientName,
        recipientPhone: order.recipientPhone,
        zipCode: order.zipCode,
        address: order.address,
        addressDetail: order.addressDetail,
      },
      payments: (order.payments || []).map((payment) => this.toPaymentDetail(payment, order)),
      memo: order.memo,
      createdAt: order.createdAt,
    };
  }

  // 취소/반품 시 주문 항목 수량만큼 재고와 판매량을 되돌린다.
  private async restoreOrderInventory(manager: EntityManager, items: OrderItem[]) {
    for (const item of items) {
      await manager.increment(Product, { id: item.productId }, 'stock', item.quantity);
      await manager.decrement(Product, { id: item.productId }, 'salesCount', item.quantity);
    }
  }

  // 주문에서 사용한 포인트를 사용자에게 환원한다.
  private async refundUsedPoints(manager: EntityManager, userId: number, pointUsed: number) {
    if (pointUsed <= 0) {
      return;
    }

    const user = await manager.findOne(User, { where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    user.point += pointUsed;
    await manager.save(user);
  }
}
