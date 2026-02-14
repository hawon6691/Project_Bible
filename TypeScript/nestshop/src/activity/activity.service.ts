import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { Order } from '../order/entities/order.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { CreateSearchHistoryDto } from './dto/create-search-history.dto';
import { RecentProductView } from './entities/recent-product-view.entity';
import { SearchHistory } from './entities/search-history.entity';

@Injectable()
export class ActivityService {
  constructor(
    @InjectRepository(RecentProductView)
    private recentProductViewRepository: Repository<RecentProductView>,
    @InjectRepository(SearchHistory)
    private searchHistoryRepository: Repository<SearchHistory>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(Order)
    private orderRepository: Repository<Order>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  // ACT-01: 활동 내역 통합 조회
  async getSummary(userId: number) {
    const [recentProducts, recentSearches, orderCount] = await Promise.all([
      this.recentProductViewRepository.find({
        where: { userId },
        relations: ['product'],
        order: { viewedAt: 'DESC' },
        take: 10,
      }),
      this.searchHistoryRepository.find({
        where: { userId },
        order: { createdAt: 'DESC' },
        take: 10,
      }),
      this.orderRepository.count({ where: { userId } }),
    ]);

    return {
      recentProducts: recentProducts.map((item) => this.toRecentProduct(item)),
      recentSearches: recentSearches.map((item) => this.toSearchHistory(item)),
      orderSummary: {
        totalOrderCount: orderCount,
      },
    };
  }

  // ACT-02: 최근 본 상품 조회
  async getRecentProducts(userId: number, query: PaginationRequestDto) {
    const [items, totalItems] = await this.recentProductViewRepository.findAndCount({
      where: { userId },
      relations: ['product'],
      order: { viewedAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    return new PaginationResponseDto(
      items.map((item) => this.toRecentProduct(item)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // 최근 본 상품 기록 추가/갱신
  async addRecentProduct(userId: number, productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const existing = await this.recentProductViewRepository.findOne({ where: { userId, productId } });
    if (existing) {
      existing.viewedAt = new Date();
      const saved = await this.recentProductViewRepository.save(existing);
      const full = await this.recentProductViewRepository.findOne({
        where: { id: saved.id },
        relations: ['product'],
      });
      return full ? this.toRecentProduct(full) : null;
    }

    const created = this.recentProductViewRepository.create({
      userId,
      productId,
      viewedAt: new Date(),
    });
    const saved = await this.recentProductViewRepository.save(created);
    const full = await this.recentProductViewRepository.findOne({
      where: { id: saved.id },
      relations: ['product'],
    });
    return full ? this.toRecentProduct(full) : null;
  }

  // ACT-03: 검색 기록 조회
  async getSearchHistory(userId: number, query: PaginationRequestDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (!user.searchHistoryEnabled) {
      return new PaginationResponseDto([], 0, query.page, query.limit);
    }

    const [items, totalItems] = await this.searchHistoryRepository.findAndCount({
      where: { userId },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    return new PaginationResponseDto(
      items.map((item) => this.toSearchHistory(item)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // 검색 기록 추가
  async addSearchHistory(userId: number, dto: CreateSearchHistoryDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (!user.searchHistoryEnabled) {
      return { saved: false, reason: 'searchHistoryDisabled' };
    }

    const keyword = dto.keyword.trim();
    if (!keyword) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '검색어는 공백일 수 없습니다.');
    }

    const item = this.searchHistoryRepository.create({
      userId,
      keyword,
    });
    const saved = await this.searchHistoryRepository.save(item);
    return this.toSearchHistory(saved);
  }

  // ACT-04: 검색 기록 삭제 (개별)
  async removeSearchHistory(userId: number, id: number) {
    const item = await this.searchHistoryRepository.findOne({ where: { id, userId } });
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.searchHistoryRepository.softRemove(item);
    return { deleted: true, id };
  }

  // ACT-04: 검색 기록 삭제 (전체)
  async clearSearchHistory(userId: number) {
    const result = await this.searchHistoryRepository.softDelete({ userId });
    return {
      deleted: true,
      affected: result.affected ?? 0,
    };
  }

  private toRecentProduct(item: RecentProductView) {
    return {
      id: item.id,
      product: item.product
        ? {
            id: item.product.id,
            name: item.product.name,
            thumbnailUrl: item.product.thumbnailUrl,
            lowestPrice: item.product.lowestPrice,
          }
        : { id: item.productId },
      viewedAt: item.viewedAt,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }

  private toSearchHistory(item: SearchHistory) {
    return {
      id: item.id,
      keyword: item.keyword,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }
}
