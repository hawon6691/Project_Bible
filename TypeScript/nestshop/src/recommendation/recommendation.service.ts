import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { Product, ProductStatus } from '../product/entities/product.entity';
import { Review } from '../review/entities/review.entity';
import { Wishlist } from '../wishlist/entities/wishlist.entity';
import { RecommendationQueryDto } from './dto/recommendation-query.dto';

@Injectable()
export class RecommendationService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(Wishlist)
    private wishlistRepository: Repository<Wishlist>,
    @InjectRepository(Review)
    private reviewRepository: Repository<Review>,
  ) {}

  async getPersonalRecommendations(userId: number, query: RecommendationQueryDto) {
  // RECO-01: 개인화 추천
  async getPersonalRecommendations(userId: number, query: RecommendationQueryDto) {
    // 1) 사용자의 찜/리뷰를 기반으로 선호 카테고리를 추출한다.
    const [wishItems, reviewItems] = await Promise.all([
      this.wishlistRepository.find({ where: { userId }, relations: ['product'] }),
      this.reviewRepository.find({ where: { userId }, relations: ['product'] }),
    ]);

    const categoryWeight = new Map<number, number>();

    for (const item of wishItems) {
      const categoryId = item.product?.categoryId;
      if (!categoryId) continue;
      categoryWeight.set(categoryId, (categoryWeight.get(categoryId) ?? 0) + 2);
    }

    for (const item of reviewItems) {
      const categoryId = item.product?.categoryId;
      if (!categoryId) continue;
      categoryWeight.set(categoryId, (categoryWeight.get(categoryId) ?? 0) + 1);
    }

    const preferredCategoryIds = [...categoryWeight.entries()]
      .sort((a, b) => b[1] - a[1])
      .map(([categoryId]) => categoryId)
      .slice(0, 5);

    const excludedProductIds = new Set<number>([
      ...wishItems.map((item) => item.productId),
      ...reviewItems.map((item) => item.productId),
    ]);

    const baseWhere = preferredCategoryIds.length
      ? { status: ProductStatus.ON_SALE, categoryId: In(preferredCategoryIds) }
      : { status: ProductStatus.ON_SALE };

    const candidates = await this.productRepository.find({
      where: baseWhere,
      order: {
        popularityScore: 'DESC',
        averageRating: 'DESC',
        reviewCount: 'DESC',
      },
      take: 200,
    });

    const items = candidates
      .filter((product) => !excludedProductIds.has(product.id))
      .slice(0, query.limit)
      .map((product, index) => this.toItem(product, index + 1));

    return {
      source: preferredCategoryIds.length ? 'personalized' : 'fallback_trending',
      items,
    };
  }

    // 2) 선호 카테고리가 있으면 카테고리 기반 추천, 없으면 인기 기반 폴백
    let candidates: Product[] = [];
    if (preferredCategoryIds.length) {
      candidates = await this.productRepository.find({
        where: {
          status: ProductStatus.ON_SALE,
          categoryId: In(preferredCategoryIds),
        },
        order: {
          popularityScore: 'DESC',
          averageRating: 'DESC',
          reviewCount: 'DESC',
        },
        take: 200,
      });
    } else {
      candidates = await this.productRepository.find({
        where: { status: ProductStatus.ON_SALE },
        order: {
          popularityScore: 'DESC',
          salesCount: 'DESC',
          viewCount: 'DESC',
        },
        take: 200,
      });
    }

    const personalized = candidates
      .filter((product) => !excludedProductIds.has(product.id))
      .slice(0, query.limit)
      .map((product, index) => this.toRecommendedProduct(product, index + 1));

    return {
      source: preferredCategoryIds.length ? 'personalized' : 'fallback_trending',
      items: personalized,
    };
  }

  // RECO-02: 비로그인/공용 추천 (인기 기반)
  async getTrendingRecommendations(query: RecommendationQueryDto) {
    const items = await this.productRepository.find({
      where: { status: ProductStatus.ON_SALE },
      order: {
        popularityScore: 'DESC',
        salesCount: 'DESC',
        viewCount: 'DESC',
      },
      take: query.limit,
    });

    return {
      source: 'trending',
      items: items.map((item, index) => this.toItem(item, index + 1)),
    };
  }

  private toItem(product: Product, rank: number) {
      items: items.map((product, index) => this.toRecommendedProduct(product, index + 1)),
    };
  }

  private toRecommendedProduct(product: Product, rank: number) {
    return {
      rank,
      productId: product.id,
      name: product.name,
      thumbnailUrl: product.thumbnailUrl,
      lowestPrice: product.lowestPrice,
      averageRating: product.averageRating,
      reviewCount: product.reviewCount,
      salesCount: product.salesCount,
      popularityScore: product.popularityScore,
      categoryId: product.categoryId,
    };
  }
}
