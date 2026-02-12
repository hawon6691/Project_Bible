import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, DataSource, In } from 'typeorm';
import { Order, OrderStatus } from './entities/order.entity';
import { OrderItem } from './entities/order-item.entity';
import { Product } from '../product/entities/product.entity';
import { Seller } from '../seller/entities/seller.entity';
import { User } from '../user/entities/user.entity';
import { CartItem } from '../cart/entities/cart-item.entity';
import { CreateOrderDto } from './dto/create-order.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';

@Injectable()
export class OrderService {
  constructor(
    @InjectRepository(Order)
    private orderRepository: Repository<Order>,
    @InjectRepository(OrderItem)
    private orderItemRepository: Repository<OrderItem>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(Seller)
    private sellerRepository: Repository<Seller>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
    private dataSource: DataSource,
  ) {}

  // ─── ORD-01: 주문 생성 (트랜잭션) ───
  async create(userId: number, dto: CreateOrderDto) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction('SERIALIZABLE');

    try {
      // 배송지 조회
      const address = await queryRunner.manager.findOne('addresses', {
        where: { id: dto.addressId, userId },
      }) as any;
      if (!address) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      // 사용자 조회 (포인트 확인)
      const user = await queryRunner.manager.findOne(User, { where: { id: userId } });
      if (!user) {
        throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      const usePoint = dto.usePoint || 0;
      if (usePoint > user.point) {
        throw new BusinessException('POINT_INSUFFICIENT', HttpStatus.BAD_REQUEST);
      }

      // 주문 상품 처리
      let totalAmount = 0;
      const orderItems: Partial<OrderItem>[] = [];

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

        // 재고 차감
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

      // 주문번호 생성
      const orderNumber = this.generateOrderNumber();

      // 주문 저장
      const order = queryRunner.manager.create(Order, {
        orderNumber,
        userId,
        status: OrderStatus.ORDER_PLACED,
        totalAmount,
        pointUsed: usePoint,
        finalAmount,
        recipientName: address.recipient_name || address.recipientName,
        recipientPhone: address.phone,
        zipCode: address.zip_code || address.zipCode,
        address: address.address,
        addressDetail: address.address_detail || address.addressDetail || null,
        memo: dto.memo || null,
      });

      const savedOrder = await queryRunner.manager.save(order);

      // 주문 상품 저장
      for (const item of orderItems) {
        const orderItem = queryRunner.manager.create(OrderItem, {
          ...item,
          orderId: savedOrder.id,
        });
        await queryRunner.manager.save(orderItem);
      }

      // 포인트 차감
      if (usePoint > 0) {
        user.point -= usePoint;
        await queryRunner.manager.save(user);
      }

      // 장바구니 삭제
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

  // ─── ORD-02: 내 주문 목록 ───
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

  // ─── ORD-03: 주문 상세 조회 ───
  async findOne(userId: number, orderId: number) {
    const order = await this.orderRepository.findOne({
      where: { id: orderId, userId },
      relations: ['items'],
    });
    if (!order) {
      throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toDetail(order);
  }

  // ─── ORD-04: 주문 취소 ───
  async cancel(userId: number, orderId: number) {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const order = await queryRunner.manager.findOne(Order, {
        where: { id: orderId, userId },
        relations: ['items'],
      });
      if (!order) {
        throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      if (order.status === OrderStatus.CANCELLED) {
        throw new BusinessException('ORDER_ALREADY_CANCELLED', HttpStatus.BAD_REQUEST);
      }

      const cancellable = [OrderStatus.ORDER_PLACED, OrderStatus.PAYMENT_PENDING];
      if (!cancellable.includes(order.status)) {
        throw new BusinessException('ORDER_CANNOT_CANCEL', HttpStatus.BAD_REQUEST);
      }

      // 재고 복원
      for (const item of order.items) {
        await queryRunner.manager.increment(Product, { id: item.productId }, 'stock', item.quantity);
        await queryRunner.manager.decrement(Product, { id: item.productId }, 'salesCount', item.quantity);
      }

      // 포인트 환원
      if (order.pointUsed > 0) {
        const user = await queryRunner.manager.findOne(User, { where: { id: userId } });
        if (user) {
          user.point += order.pointUsed;
          await queryRunner.manager.save(user);
        }
      }

      order.status = OrderStatus.CANCELLED;
      await queryRunner.manager.save(order);

      await queryRunner.commitTransaction();
      return this.toDetail(order);
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  // ─── ORD-05: 전체 주문 관리 (Admin) ───
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

  // ─── ORD-06: 주문 상태 변경 (Admin) ───
  async updateStatus(orderId: number, status: OrderStatus) {
    const order = await this.orderRepository.findOne({
      where: { id: orderId },
      relations: ['items'],
    });
    if (!order) {
      throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    order.status = status;
    const saved = await this.orderRepository.save(order);
    return this.toDetail(saved);
  }

  // ─── 헬퍼: 주문번호 생성 ───
  private generateOrderNumber(): string {
    const now = new Date();
    const date = now.toISOString().slice(0, 10).replace(/-/g, '');
    const random = Math.random().toString(36).substring(2, 8).toUpperCase();
    return `ORD-${date}-${random}`;
  }

  // ─── 헬퍼: 요약 ───
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

  // ─── 헬퍼: 상세 ───
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
      memo: order.memo,
      createdAt: order.createdAt,
    };
  }
}
