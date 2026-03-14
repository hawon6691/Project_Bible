import { HttpStatus, Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { ElasticsearchService } from '@nestjs/elasticsearch';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
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

interface SearchDocument {
  name: string;
  description: string;
  categoryId: number;
  lowestPrice: number | null;
  averageRating: number;
  popularityScore: number;
  sellerIds: number[];
  suggest: {
    input: string[];
    weight: number;
  };
}

@Injectable()
export class SearchService implements OnModuleInit {
  private readonly logger = new Logger(SearchService.name);
  private readonly indexName = 'products_v1';
  private readonly defaultWeights: Record<string, number> = {
    name: 10,
    description: 1,
    spec: 3,
    popularity: 2,
  };

  constructor(
    private readonly elasticsearchService: ElasticsearchService,
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

  async onModuleInit() {
    try {
      await this.ensureIndex();
    } catch (error) {
      // ES 버전/연결 문제로 인덱스 준비에 실패해도 서비스 전체 부팅은 유지한다.
      const message = error instanceof Error ? error.message : 'unknown error';
      this.logger.warn(`Elasticsearch index bootstrap skipped: ${message}`);
    }
  }

  async search(query: SearchQueryDto) {
    const keyword = (query.keyword ?? '').trim();

    try {
      const esResult = await this.searchByElasticsearch(query, keyword);
      if (keyword) {
        await this.logSearch(keyword, esResult.meta.totalItems, {
          categoryId: query.categoryId,
          sellerId: query.sellerId,
          minPrice: query.minPrice,
          maxPrice: query.maxPrice,
          minRating: query.minRating,
        });
      }
      return esResult;
    } catch {
      // Elasticsearch 장애 시 DB 검색으로 폴백해 API 가용성을 유지한다.
      return this.searchByDatabase(query, keyword);
    }
  }

  async autocomplete(query: SearchAutocompleteQueryDto) {
    const q = (query.q ?? '').trim();
    if (!q) return { items: [] };

    try {
      const suggestResponse = await this.elasticsearchService.search<any>({
        index: this.indexName,
        size: 0,
        suggest: {
          product_suggest: {
            prefix: q,
            completion: {
              field: 'suggest',
              fuzzy: { fuzziness: 'AUTO' },
              size: query.limit,
            },
          },
        },
      });

      const options = (suggestResponse.suggest?.product_suggest?.[0]?.options ?? []) as any[];
      const keywords = [...new Set(options.map((option) => String(option.text)))].slice(0, query.limit);

      return {
        items: keywords.map((keyword: string) => ({
          keyword,
          highlighted: this.highlightKeyword(keyword, q),
        })),
      };
    } catch {
      return this.autocompleteByDatabase(query, q);
    }
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
      item = this.searchRecentKeywordRepository.create({ userId, keyword, lastSearchedAt: new Date() });
    } else {
      item.lastSearchedAt = new Date();
    }
    await this.searchRecentKeywordRepository.save(item);

    const rows = await this.searchRecentKeywordRepository.find({
      where: { userId },
      order: { lastSearchedAt: 'DESC' },
    });
    if (rows.length > 10) {
      await this.searchRecentKeywordRepository.softRemove(rows.slice(10));
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

    return { searchHistoryEnabled: user.searchHistoryEnabled };
  }

  async getWeights() {
    const setting = await this.getOrCreateWeightSetting();
    return { weights: setting.weights, updatedAt: setting.updatedAt };
  }

  async updateWeights(dto: UpdateSearchWeightDto) {
    const sanitized = this.sanitizeWeights(dto.weights);
    const setting = await this.getOrCreateWeightSetting();
    setting.weights = sanitized;
    const saved = await this.searchWeightSettingRepository.save(setting);
    return { weights: saved.weights, updatedAt: saved.updatedAt };
  }

  async getIndexStatus() {
    const exists = await this.elasticsearchService.indices.exists({ index: this.indexName });
    const count = exists ? await this.elasticsearchService.count({ index: this.indexName }) : { count: 0 };
    return {
      index: this.indexName,
      exists,
      documentCount: count.count,
    };
  }

  async reindexAllProducts() {
    await this.ensureIndex();
    const products = await this.productRepository.find();
    await this.bulkUpsertDocuments(products);
    return { success: true, indexedCount: products.length };
  }

  async reindexProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.upsertDocument(product);
    return { success: true, productId };
  }

  async removeProductDocument(productId: number) {
    try {
      await this.elasticsearchService.delete({
        index: this.indexName,
        id: String(productId),
        refresh: true,
      });
    } catch {
      // 이미 삭제된 문서는 무시한다.
    }

    return { success: true, productId };
  }

  private async searchByElasticsearch(query: SearchQueryDto, keyword: string) {
    const weights = (await this.getOrCreateWeightSetting()).weights;
    const must: any[] = [];
    const filter: any[] = [];

    if (keyword) {
      must.push({
        multi_match: {
          query: keyword,
          type: 'best_fields',
          fuzziness: 'AUTO',
          operator: 'and',
          fields: [
            `name^${weights.name ?? this.defaultWeights.name}`,
            `description^${weights.description ?? this.defaultWeights.description}`,
          ],
        },
      });
    } else {
      must.push({ match_all: {} });
    }

    if (query.categoryId !== undefined) {
      filter.push({ term: { categoryId: query.categoryId } });
    }
    if (query.minPrice !== undefined || query.maxPrice !== undefined) {
      const range: Record<string, number> = {};
      if (query.minPrice !== undefined) range.gte = query.minPrice;
      if (query.maxPrice !== undefined) range.lte = query.maxPrice;
      filter.push({ range: { lowestPrice: range } });
    }
    if (query.minRating !== undefined) {
      filter.push({ range: { averageRating: { gte: query.minRating } } });
    }
    if (query.sellerId !== undefined) {
      filter.push({ term: { sellerIds: query.sellerId } });
    }

    const response = await this.elasticsearchService.search<any>({
      index: this.indexName,
      from: query.skip,
      size: query.limit,
      query: { bool: { must, filter } },
      sort: [{ _score: { order: 'desc' } }, { popularityScore: { order: 'desc' } }],
    });

    const hits = response.hits?.hits ?? [];
    const ids = hits.map((hit: any) => Number(hit._id)).filter((id: number) => Number.isFinite(id));
    const total = typeof response.hits?.total === 'number' ? response.hits.total : (response.hits?.total?.value ?? 0);

    const products = ids.length ? await this.productRepository.find({ where: { id: In(ids) } }) : [];
    const productMap = new Map(products.map((item) => [item.id, item]));
    const ordered = ids.map((id) => productMap.get(id)).filter((item): item is Product => !!item);

    const result = new PaginationResponseDto(
      ordered.map((item) => this.toSearchItem(item)),
      total,
      query.page,
      query.limit,
    );

    return {
      ...result,
      relatedKeywords: await this.getRelatedKeywords(keyword),
      engine: 'elasticsearch',
    };
  }

  private async searchByDatabase(query: SearchQueryDto, keyword: string) {
    const qb = this.productRepository.createQueryBuilder('product').orderBy('product.popularityScore', 'DESC');

    if (keyword) {
      qb.andWhere('(product.name ILIKE :keyword OR product.description ILIKE :keyword)', {
        keyword: `%${keyword}%`,
      });
    }
    if (query.categoryId !== undefined) qb.andWhere('product.categoryId = :categoryId', { categoryId: query.categoryId });
    if (query.minPrice !== undefined) {
      qb.andWhere('(COALESCE(product.lowestPrice, product.price) >= :minPrice)', { minPrice: query.minPrice });
    }
    if (query.maxPrice !== undefined) {
      qb.andWhere('(COALESCE(product.lowestPrice, product.price) <= :maxPrice)', { maxPrice: query.maxPrice });
    }
    if (query.minRating !== undefined) qb.andWhere('product.averageRating >= :minRating', { minRating: query.minRating });

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
          engine: 'database',
        };
      }
      qb.andWhere('product.id IN (:...ids)', { ids });
    }

    qb.skip(query.skip).take(query.limit);
    const [items, total] = await qb.getManyAndCount();

    if (keyword) {
      await this.logSearch(keyword, total, {
        categoryId: query.categoryId,
        sellerId: query.sellerId,
        minPrice: query.minPrice,
        maxPrice: query.maxPrice,
        minRating: query.minRating,
      });
    }

    const result = new PaginationResponseDto(
      items.map((item) => this.toSearchItem(item)),
      total,
      query.page,
      query.limit,
    );

    return {
      ...result,
      relatedKeywords: await this.getRelatedKeywords(keyword),
      engine: 'database',
    };
  }

  private async autocompleteByDatabase(query: SearchAutocompleteQueryDto, q: string) {
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
      engine: 'database',
    };
  }

  private async ensureIndex() {
    const exists = await this.elasticsearchService.indices.exists({ index: this.indexName });
    if (exists) return;

    await this.elasticsearchService.indices.create({
      index: this.indexName,
      settings: {
        analysis: {
          tokenizer: {
            edge_ngram_tokenizer: {
              type: 'edge_ngram',
              min_gram: 1,
              max_gram: 20,
              token_chars: ['letter', 'digit'],
            },
          },
          analyzer: {
            nori_analyzer: {
              type: 'custom',
              tokenizer: 'nori_tokenizer',
              filter: ['lowercase'],
            },
            autocomplete_analyzer: {
              type: 'custom',
              tokenizer: 'edge_ngram_tokenizer',
              filter: ['lowercase'],
            },
          },
        },
      },
      mappings: {
        properties: {
          name: {
            type: 'text',
            analyzer: 'nori_analyzer',
            fields: { keyword: { type: 'keyword' } },
          },
          description: { type: 'text', analyzer: 'nori_analyzer' },
          categoryId: { type: 'integer' },
          lowestPrice: { type: 'integer' },
          averageRating: { type: 'float' },
          popularityScore: { type: 'float' },
          sellerIds: { type: 'integer' },
          suggest: { type: 'completion' },
        },
      },
    });
  }

  private async bulkUpsertDocuments(products: Product[]) {
    if (!products.length) return;

    const productIds = products.map((item) => item.id);
    const entries = await this.priceEntryRepository.find({
      where: { productId: In(productIds), isAvailable: true },
    });

    const byProduct = new Map<number, number[]>();
    for (const entry of entries) {
      const prev = byProduct.get(entry.productId) ?? [];
      prev.push(entry.sellerId);
      byProduct.set(entry.productId, prev);
    }

    const operations: any[] = [];
    for (const product of products) {
      const document = this.toDocument(product, byProduct.get(product.id) ?? []);
      operations.push({ index: { _index: this.indexName, _id: String(product.id) } });
      operations.push(document);
    }

    await this.elasticsearchService.bulk({ refresh: true, operations });
  }

  private async upsertDocument(product: Product) {
    const entries = await this.priceEntryRepository.find({
      where: { productId: product.id, isAvailable: true },
    });
    const sellerIds = [...new Set(entries.map((item) => item.sellerId))];
    const document = this.toDocument(product, sellerIds);

    await this.elasticsearchService.index({
      index: this.indexName,
      id: String(product.id),
      refresh: true,
      document,
    });
  }

  private toDocument(product: Product, sellerIds: number[]): SearchDocument {
    const popularity = Number(product.popularityScore ?? 0);
    const lowestPrice = product.lowestPrice ?? product.price ?? null;
    const averageRating = Number(product.averageRating ?? 0);

    return {
      name: product.name,
      description: product.description ?? '',
      categoryId: product.categoryId,
      lowestPrice,
      averageRating,
      popularityScore: popularity,
      sellerIds,
      suggest: {
        input: [product.name],
        weight: Math.max(1, Math.round(popularity)),
      },
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
    if (!keyword) return [];

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
