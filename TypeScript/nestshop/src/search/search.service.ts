import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { PopularSearchQueryDto } from './dto/popular-search-query.dto';
import { SaveRecentSearchDto } from './dto/save-recent-search.dto';
import { SearchAutocompleteQueryDto } from './dto/search-autocomplete-query.dto';
import { SearchQueryDto } from './dto/search-query.dto';
import { UpdateSearchPreferenceDto } from './dto/update-search-preference.dto';
import { UpdateSearchWeightDto } from './dto/update-search-weight.dto';
import { SearchLog } from './entities/search-log.entity';
import { SearchRecentKeyword } from './entities/search-recent-keyword.entity';
import { SearchWeightSetting } from './entities/search-weight-setting.entity';

@Injectable()
export class SearchService {
  private readonly defaultWeights: Record<string, number> = {
    name: 1.8,
    description: 1.1,
    spec: 1.3,
    popularity: 1.2,
  };

  constructor(
    @InjectRepository(Product)
    private readonly productRepository: Repository<Product>,
    @InjectRepository(PriceEntry)
    private readonly priceEntryRepository: Repository<PriceEntry>,
    @InjectRepository(User)
    private readonly userRepository: Repository<User>,
    @InjectRepository(SearchLog)
    private readonly searchLogRepository: Repository<SearchLog>,
    @InjectRepository(SearchRecentKeyword)
    private readonly searchRecentKeywordRepository: Repository<SearchRecentKeyword>,
    @InjectRepository(SearchWeightSetting)
    private readonly searchWeightSettingRepository: Repository<SearchWeightSetting>,
  ) {}

  async search(query: SearchQueryDto) {
    const keyword = (query.keyword ?? '').trim();
    const qb = this.productRepository.createQueryBuilder('product').orderBy('product.popularityScore', 'DESC');

    if (keyword) {
      qb.andWhere('(product.name ILIKE :keyword OR product.description ILIKE :keyword)', {
        keyword: `%${keyword}%`,
      });
    }

    if (query.categoryId !== undefined) {
      qb.andWhere('product.categoryId = :categoryId', { categoryId: query.categoryId });
    }

    if (query.minPrice !== undefined) {
      qb.andWhere('(COALESCE(product.lowestPrice, product.price) >= :minPrice)', { minPrice: query.minPrice });
    }

    if (query.maxPrice !== undefined) {
      qb.andWhere('(COALESCE(product.lowestPrice, product.price) <= :maxPrice)', { maxPrice: query.maxPrice });
    }

    if (query.minRating !== undefined) {
      qb.andWhere('product.averageRating >= :minRating', { minRating: query.minRating });
    }

    if (query.sellerId !== undefined) {
      const productIds = await this.priceEntryRepository
        .createQueryBuilder('entry')
        .select('entry.productId', 'productId')
        .where('entry.sellerId = :sellerId', { sellerId: query.sellerId })
        .andWhere('entry.isAvailable = true')
        .groupBy('entry.productId')
        .getRawMany<{ productId: number }>();

      const ids = productIds.map((item) => Number(item.productId));
      if (!ids.length) {
        return {
          items: [],
          meta: {
            totalItems: 0,
            itemCount: 0,
            itemsPerPage: query.limit,
            totalPages: 0,
            currentPage: query.page,
          },
          relatedKeywords: await this.getRelatedKeywords(keyword),
        };
      }

      qb.andWhere('product.id IN (:...ids)', { ids });
    }

    qb.skip(query.skip).take(query.limit);

    const [items, total] = await qb.getManyAndCount();
    const mapped = items.map((item) => this.toSearchItem(item));

    if (keyword) {
      await this.logSearch(keyword, total, {
        categoryId: query.categoryId,
        sellerId: query.sellerId,
        minPrice: query.minPrice,
        maxPrice: query.maxPrice,
        minRating: query.minRating,
      });
    }

    const response = new PaginationResponseDto(mapped, total, query.page, query.limit);
    return {
      ...response,
      relatedKeywords: await this.getRelatedKeywords(keyword),
    };
  }

  async autocomplete(query: SearchAutocompleteQueryDto) {
    const q = (query.q ?? '').trim();
    if (!q) {
      return { items: [] };
    }

    const products = await this.productRepository
      .createQueryBuilder('product')
      .select(['product.name as keyword'])
      .where('product.name ILIKE :q', { q: `%${q}%` })
      .orderBy('product.popularityScore', 'DESC')
      .limit(query.limit)
      .getRawMany<{ keyword: string }>();

    const logs = await this.searchLogRepository
      .createQueryBuilder('log')
      .select('log.keyword', 'keyword')
      .addSelect('COUNT(*)', 'count')
      .where('log.keyword ILIKE :q', { q: `%${q}%` })
      .groupBy('log.keyword')
      .orderBy('count', 'DESC')
      .limit(query.limit)
      .getRawMany<{ keyword: string }>();

    const unique = [...new Set([...products.map((item) => item.keyword), ...logs.map((item) => item.keyword)])].slice(
      0,
      query.limit,
    );

    return {
      items: unique.map((keyword) => ({
        keyword,
        highlighted: this.highlightKeyword(keyword, q),
      })),
    };
  }

  async getPopularKeywords(query: PopularSearchQueryDto) {
    const rows = await this.searchLogRepository
      .createQueryBuilder('log')
      .select('log.keyword', 'keyword')
      .addSelect('COUNT(*)', 'count')
      .where("log.createdAt >= NOW() - INTERVAL '7 days'")
      .groupBy('log.keyword')
      .orderBy('count', 'DESC')
      .limit(query.limit)
      .getRawMany<{ keyword: string; count: string }>();

    return {
      items: rows.map((item, index) => ({
        rank: index + 1,
        keyword: item.keyword,
        count: Number(item.count),
      })),
    };
  }

  async saveRecentKeyword(userId: number, dto: SaveRecentSearchDto) {
    const user = await this.ensureUser(userId);
    if (!user.searchHistoryEnabled) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '검색어 자동 저장이 비활성화되어 있습니다.');
    }

    const keyword = dto.keyword.trim();
    if (!keyword) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '검색어를 입력해주세요.');
    }

    let item = await this.searchRecentKeywordRepository.findOne({ where: { userId, keyword } });

    if (!item) {
      item = this.searchRecentKeywordRepository.create({
        userId,
        keyword,
        lastSearchedAt: new Date(),
      });
    } else {
      item.lastSearchedAt = new Date();
    }

    await this.searchRecentKeywordRepository.save(item);

    const rows = await this.searchRecentKeywordRepository.find({
      where: { userId },
      order: { lastSearchedAt: 'DESC' },
    });

    if (rows.length > 10) {
      const overflow = rows.slice(10);
      await this.searchRecentKeywordRepository.softRemove(overflow);
    }

    return this.getRecentKeywords(userId);
  }

  async getRecentKeywords(userId: number) {
    await this.ensureUser(userId);

    const items = await this.searchRecentKeywordRepository.find({
      where: { userId },
      order: { lastSearchedAt: 'DESC' },
      take: 10,
    });

    return {
      items: items.map((item) => ({
        id: item.id,
        keyword: item.keyword,
        lastSearchedAt: item.lastSearchedAt,
      })),
    };
  }

  async removeRecentKeyword(userId: number, id: number) {
    const item = await this.searchRecentKeywordRepository.findOne({ where: { id, userId } });
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '최근 검색어를 찾을 수 없습니다.');
    }

    await this.searchRecentKeywordRepository.softDelete({ id });
    return { success: true, message: '최근 검색어가 삭제되었습니다.' };
  }

  async clearRecentKeywords(userId: number) {
    await this.searchRecentKeywordRepository.softDelete({ userId });
    return { success: true, message: '최근 검색어가 전체 삭제되었습니다.' };
  }

  async updatePreference(userId: number, dto: UpdateSearchPreferenceDto) {
    const user = await this.ensureUser(userId);
    user.searchHistoryEnabled = dto.searchHistoryEnabled;
    await this.userRepository.save(user);

    return {
      searchHistoryEnabled: user.searchHistoryEnabled,
    };
  }

  async getWeights() {
    const setting = await this.getOrCreateWeightSetting();
    return {
      weights: setting.weights,
      updatedAt: setting.updatedAt,
    };
  }

  async updateWeights(dto: UpdateSearchWeightDto) {
    const sanitized = this.sanitizeWeights(dto.weights);
    const setting = await this.getOrCreateWeightSetting();
    setting.weights = sanitized;
    const saved = await this.searchWeightSettingRepository.save(setting);

    return {
      weights: saved.weights,
      updatedAt: saved.updatedAt,
    };
  }

  private async logSearch(keyword: string, resultCount: number, filters: Record<string, unknown>) {
    const log = this.searchLogRepository.create({
      userId: null,
      keyword,
      resultCount,
      clickedProductId: null,
      filters,
    });

    await this.searchLogRepository.save(log);
  }

  private async getRelatedKeywords(keyword: string) {
    if (!keyword) {
      return [];
    }

    const rows = await this.searchLogRepository
      .createQueryBuilder('log')
      .select('log.keyword', 'keyword')
      .addSelect('COUNT(*)', 'count')
      .where('log.keyword ILIKE :keyword', { keyword: `%${keyword}%` })
      .andWhere('log.keyword != :exact', { exact: keyword })
      .groupBy('log.keyword')
      .orderBy('count', 'DESC')
      .limit(5)
      .getRawMany<{ keyword: string }>();

    return rows.map((row) => row.keyword);
  }

  private highlightKeyword(keyword: string, q: string) {
    const escaped = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const pattern = new RegExp(`(${escaped})`, 'ig');
    return keyword.replace(pattern, '<em>$1</em>');
  }

  private sanitizeWeights(weights: Record<string, number>) {
    const entries = Object.entries(weights)
      .filter(([key, value]) => !!key && Number.isFinite(value) && value >= 0)
      .map(([key, value]) => [key, Number(value)] as const);

    if (!entries.length) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '유효한 가중치가 없습니다.');
    }

    return Object.fromEntries(entries);
  }

  private async getOrCreateWeightSetting() {
    let setting = await this.searchWeightSettingRepository.findOne({ where: { settingName: 'default' } });
    if (!setting) {
      setting = this.searchWeightSettingRepository.create({
        settingName: 'default',
        weights: this.defaultWeights,
      });
      setting = await this.searchWeightSettingRepository.save(setting);
    }

    return setting;
  }

  private async ensureUser(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '사용자를 찾을 수 없습니다.');
    }

    return user;
  }

  private toSearchItem(item: Product) {
    return {
      id: item.id,
      name: item.name,
      thumbnailUrl: item.thumbnailUrl,
      lowestPrice: item.lowestPrice,
      averageRating: Number(item.averageRating),
      reviewCount: item.reviewCount,
      popularityScore: Number(item.popularityScore),
    };
  }
}
