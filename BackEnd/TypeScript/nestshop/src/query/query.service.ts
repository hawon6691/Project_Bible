import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository, SelectQueryBuilder } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { ProductQuerySort, ProductQueryViewDto } from './dto/product-query-view.dto';
import { ProductQueryView } from './entities/product-query-view.entity';

@Injectable()
export class QueryService {
  constructor(
    @InjectRepository(ProductQueryView)
    private readonly productQueryViewRepository: Repository<ProductQueryView>,
    @InjectRepository(Product)
    private readonly productRepository: Repository<Product>,
    @InjectRepository(PriceEntry)
    private readonly priceEntryRepository: Repository<PriceEntry>,
  ) {}

  async findProducts(query: ProductQueryViewDto) {
    const qb = this.productQueryViewRepository.createQueryBuilder('qv');

    if (query.categoryId !== undefined) {
      qb.andWhere('qv.categoryId = :categoryId', { categoryId: query.categoryId });
    }
    if (query.keyword) {
      qb.andWhere('qv.name ILIKE :keyword', { keyword: `%${query.keyword}%` });
    }
    if (query.minPrice !== undefined) {
      qb.andWhere('COALESCE(qv.lowestPrice, qv.basePrice) >= :minPrice', { minPrice: query.minPrice });
    }
    if (query.maxPrice !== undefined) {
      qb.andWhere('COALESCE(qv.lowestPrice, qv.basePrice) <= :maxPrice', { maxPrice: query.maxPrice });
    }

    this.applySort(qb, query.sort ?? ProductQuerySort.NEWEST);

    const [items, total] = await qb.skip(query.skip).take(query.limit).getManyAndCount();
    return new PaginationResponseDto(items.map((item) => this.toResponse(item)), total, query.page, query.limit);
  }

  async findProductDetail(productId: number) {
    const item = await this.productQueryViewRepository.findOne({ where: { productId } });
    if (!item) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toResponse(item);
  }

  async syncProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const [activeEntries, existing] = await Promise.all([
      this.priceEntryRepository.find({ where: { productId, isAvailable: true } }),
      this.productQueryViewRepository.findOne({ where: { productId } }),
    ]);

    const lowestPrice = activeEntries.length ? Math.min(...activeEntries.map((entry) => entry.price)) : null;

    const entity = existing
      ? existing
      : this.productQueryViewRepository.create({
          productId,
          categoryId: product.categoryId,
          name: product.name,
          thumbnailUrl: product.thumbnailUrl,
          status: product.status,
          basePrice: product.price,
          lowestPrice,
          sellerCount: activeEntries.length,
          averageRating: Number(product.averageRating),
          reviewCount: product.reviewCount,
          viewCount: product.viewCount,
          popularityScore: Number(product.popularityScore),
          syncedAt: new Date(),
        });

    entity.categoryId = product.categoryId;
    entity.name = product.name;
    entity.thumbnailUrl = product.thumbnailUrl;
    entity.status = product.status;
    entity.basePrice = product.price;
    entity.lowestPrice = lowestPrice;
    entity.sellerCount = activeEntries.length;
    entity.averageRating = Number(product.averageRating);
    entity.reviewCount = product.reviewCount;
    entity.viewCount = product.viewCount;
    entity.popularityScore = Number(product.popularityScore);
    entity.syncedAt = new Date();

    const saved = await this.productQueryViewRepository.save(entity);
    return this.toResponse(saved);
  }

  // 쓰기 모델 전체를 읽기 모델로 다시 투영한다.
  async rebuildAll() {
    const products = await this.productRepository.find({ select: ['id'] });
    let syncedCount = 0;

    for (const product of products) {
      await this.syncProduct(product.id);
      syncedCount += 1;
    }

    return { syncedCount };
  }

  private applySort(qb: SelectQueryBuilder<ProductQueryView>, sort: ProductQuerySort) {
    switch (sort) {
      case ProductQuerySort.PRICE_ASC:
        qb.orderBy('COALESCE(qv.lowestPrice, qv.basePrice)', 'ASC');
        break;
      case ProductQuerySort.PRICE_DESC:
        qb.orderBy('COALESCE(qv.lowestPrice, qv.basePrice)', 'DESC');
        break;
      case ProductQuerySort.POPULARITY:
        qb.orderBy('qv.popularityScore', 'DESC').addOrderBy('qv.viewCount', 'DESC');
        break;
      case ProductQuerySort.RATING:
        qb.orderBy('qv.averageRating', 'DESC').addOrderBy('qv.reviewCount', 'DESC');
        break;
      case ProductQuerySort.NEWEST:
      default:
        qb.orderBy('qv.updatedAt', 'DESC');
        break;
    }
  }

  private toResponse(item: ProductQueryView) {
    return {
      productId: item.productId,
      categoryId: item.categoryId,
      name: item.name,
      thumbnailUrl: item.thumbnailUrl,
      status: item.status,
      basePrice: item.basePrice,
      lowestPrice: item.lowestPrice,
      sellerCount: item.sellerCount,
      averageRating: Number(item.averageRating),
      reviewCount: item.reviewCount,
      viewCount: item.viewCount,
      popularityScore: Number(item.popularityScore),
      syncedAt: item.syncedAt,
      updatedAt: item.updatedAt,
    };
  }
}
