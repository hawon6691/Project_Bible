import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { DataSource, EntityManager, Repository } from 'typeorm';
import { Review } from './entities/review.entity';
import { CreateReviewDto } from './dto/create-review.dto';
import { UpdateReviewDto } from './dto/update-review.dto';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { Order, OrderStatus } from '../order/entities/order.entity';
import { OrderItem } from '../order/entities/order-item.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { UserRole } from '../common/decorators/roles.decorator';

const REVIEW_REWARD_POINT = 500;

@Injectable()
export class ReviewService {
  constructor(
    @InjectRepository(Review)
    private reviewRepository: Repository<Review>,
    @InjectRepository(Order)
    private orderRepository: Repository<Order>,
    @InjectRepository(OrderItem)
    private orderItemRepository: Repository<OrderItem>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
    private dataSource: DataSource,
  ) {}

  // REV-04: 상품 리뷰 목록
  async findByProduct(productId: number, query: PaginationRequestDto) {
    const [items, totalItems] = await this.reviewRepository.findAndCount({
      where: { productId },
      relations: ['user'],
      skip: query.skip,
      take: query.limit,
      order: { createdAt: 'DESC' },
    });

    const mapped = items.map((review) => ({
      id: review.id,
      productId: review.productId,
      orderId: review.orderId,
      rating: review.rating,
      content: review.content,
      user: {
        id: review.user.id,
        nickname: review.user.nickname,
      },
      createdAt: review.createdAt,
      updatedAt: review.updatedAt,
    }));

    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // REV-01: 리뷰 작성 (구매 확정 주문만 가능)
  async create(userId: number, productId: number, dto: CreateReviewDto) {
    return this.dataSource.transaction(async (manager) => {
      const order = await manager.findOne(Order, { where: { id: dto.orderId, userId } });
      if (!order) {
        throw new BusinessException('ORDER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }
      if (order.status !== OrderStatus.CONFIRMED) {
        throw new BusinessException(
          'ORDER_CANNOT_CANCEL',
          HttpStatus.BAD_REQUEST,
          '구매 확정된 주문만 리뷰를 작성할 수 있습니다.',
        );
      }

      const orderItem = await manager.findOne(OrderItem, {
        where: { orderId: dto.orderId, productId },
      });
      if (!orderItem) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }
      if (orderItem.isReviewed) {
        throw new BusinessException('REVIEW_ALREADY_EXISTS', HttpStatus.BAD_REQUEST);
      }

      const existingReview = await manager.findOne(Review, {
        where: { userId, orderId: dto.orderId, productId },
      });
      if (existingReview) {
        throw new BusinessException('REVIEW_ALREADY_EXISTS', HttpStatus.BAD_REQUEST);
      }

      const review = manager.create(Review, {
        userId,
        productId,
        orderId: dto.orderId,
        rating: dto.rating,
        content: dto.content,
      });
      const savedReview = await manager.save(review);

      orderItem.isReviewed = true;
      await manager.save(orderItem);

      const user = await manager.findOne(User, { where: { id: userId } });
      if (user) {
        user.point += REVIEW_REWARD_POINT;
        await manager.save(user);
      }

      await this.recalculateProductReviewStats(manager, productId);

      return this.reviewRepository.findOne({
        where: { id: savedReview.id },
        relations: ['user'],
      });
    });
  }

  // REV-02: 리뷰 수정
  async update(userId: number, reviewId: number, dto: UpdateReviewDto) {
    return this.dataSource.transaction(async (manager) => {
      const review = await manager.findOne(Review, { where: { id: reviewId } });
      if (!review) {
        throw new BusinessException('REVIEW_NOT_FOUND', HttpStatus.NOT_FOUND);
      }
      if (review.userId !== userId) {
        throw new BusinessException('REVIEW_NOT_OWNER', HttpStatus.FORBIDDEN);
      }

      const originalProductId = review.productId;
      if (dto.rating !== undefined) review.rating = dto.rating;
      if (dto.content !== undefined) review.content = dto.content;
      await manager.save(review);

      await this.recalculateProductReviewStats(manager, originalProductId);

      return manager.findOne(Review, {
        where: { id: review.id },
        relations: ['user'],
      });
    });
  }

  // REV-03: 리뷰 삭제 (작성자/관리자)
  async remove(actorId: number, role: UserRole, reviewId: number) {
    return this.dataSource.transaction(async (manager) => {
      const review = await manager.findOne(Review, { where: { id: reviewId } });
      if (!review) {
        throw new BusinessException('REVIEW_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      const isOwner = review.userId === actorId;
      const isAdmin = role === UserRole.ADMIN;
      if (!isOwner && !isAdmin) {
        throw new BusinessException('REVIEW_NOT_OWNER', HttpStatus.FORBIDDEN);
      }

      await manager.softRemove(review);
      await this.recalculateProductReviewStats(manager, review.productId);

      return { message: '리뷰가 삭제되었습니다.' };
    });
  }

  private async recalculateProductReviewStats(manager: EntityManager, productId: number) {
    const raw = await manager
      .getRepository(Review)
      .createQueryBuilder('review')
      .select('COUNT(review.id)', 'count')
      .addSelect('COALESCE(AVG(review.rating), 0)', 'avg')
      .where('review.product_id = :productId', { productId })
      .andWhere('review.deleted_at IS NULL')
      .getRawOne<{ count: string; avg: string }>();

    const reviewCount = Number(raw?.count || 0);
    const averageRating = reviewCount > 0 ? Number(raw?.avg || 0) : 0;

    await manager.update(Product, { id: productId }, {
      reviewCount,
      averageRating: Number(averageRating.toFixed(1)),
    });
  }
}
