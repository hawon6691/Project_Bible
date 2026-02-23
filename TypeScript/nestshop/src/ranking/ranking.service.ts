import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { SearchHistory } from '../activity/entities/search-history.entity';
import { CACHE_KEYS, CACHE_KEY_PREFIX, CACHE_TTL_SECONDS } from '../common/cache/cache-policy.constants';
import { CacheService } from '../common/cache/cache.service';
import { Product } from '../product/entities/product.entity';
import { RankingQueryDto } from './dto/ranking-query.dto';

@Injectable()
export class RankingService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(SearchHistory)
    private searchHistoryRepository: Repository<SearchHistory>,
    private readonly cacheService: CacheService,
  ) {}

  // 인기 상품 랭킹 조회
  async getPopularProducts(query: RankingQueryDto) {
    const cacheKey = CACHE_KEYS.rankingProducts(query.limit);
    const cached = await this.cacheService.getJson<ReturnType<RankingService['toProductRankingItems']>>(cacheKey);
    if (cached) {
      return cached;
    }

    const items = await this.productRepository.find({
      order: {
        popularityScore: 'DESC',
        salesCount: 'DESC',
        viewCount: 'DESC',
      },
      take: query.limit,
    });

    const result = this.toProductRankingItems(items);
    await this.cacheService.setJson(cacheKey, result, CACHE_TTL_SECONDS.RANKING_PRODUCTS);
    return result;
  }

  // 인기 검색어 랭킹 조회
  async getPopularKeywords(query: RankingQueryDto) {
    const cacheKey = CACHE_KEYS.rankingKeywords(query.limit);
    const cached = await this.cacheService.getJson<ReturnType<RankingService['toKeywordRankingItems']>>(cacheKey);
    if (cached) {
      return cached;
    }

    const raw = await this.searchHistoryRepository
      .createQueryBuilder('search')
      .select('search.keyword', 'keyword')
      .addSelect('COUNT(search.id)', 'count')
      .groupBy('search.keyword')
      .orderBy('count', 'DESC')
      .addOrderBy('search.keyword', 'ASC')
      .limit(query.limit)
      .getRawMany<{ keyword: string; count: string }>();

    const result = this.toKeywordRankingItems(raw);
    await this.cacheService.setJson(cacheKey, result, CACHE_TTL_SECONDS.RANKING_KEYWORDS);
    return result;
  }

  // 인기 점수 재계산: (조회수*0.3) + (리뷰수*0.5) + (판매량*0.2)
  async recalculatePopularityScore() {
    const products = await this.productRepository.find();

    for (const product of products) {
      const score = product.viewCount * 0.3 + product.reviewCount * 0.5 + product.salesCount * 0.2;
      product.popularityScore = Number(score.toFixed(2));
      await this.productRepository.save(product);
    }

    await this.cacheService.delByPattern(`${CACHE_KEY_PREFIX.RANKING}:*`);

    return { updatedCount: products.length };
  }

  private toProductRankingItems(items: Product[]) {
    return items.map((item, index) => ({
      rank: index + 1,
      productId: item.id,
      name: item.name,
      thumbnailUrl: item.thumbnailUrl,
      lowestPrice: item.lowestPrice,
      viewCount: item.viewCount,
      salesCount: item.salesCount,
      reviewCount: item.reviewCount,
      averageRating: item.averageRating,
      popularityScore: item.popularityScore,
    }));
  }

  private toKeywordRankingItems(raw: { keyword: string; count: string }[]) {
    return raw.map((item, index) => ({
      rank: index + 1,
      keyword: item.keyword,
      count: Number(item.count),
    }));
  }
}
