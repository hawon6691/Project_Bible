import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { Product } from '../product/entities/product.entity';
import { AddCompareItemDto } from './dto/add-compare-item.dto';
import { CompareItem } from './entities/compare-item.entity';

@Injectable()
export class CompareService {
  private static readonly MAX_COMPARE_ITEMS = 4;

  constructor(
    @InjectRepository(CompareItem)
    private compareItemRepository: Repository<CompareItem>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  async add(compareKey: string, dto: AddCompareItemDto) {
    const normalizedKey = this.normalizeCompareKey(compareKey);
    await this.ensureProduct(dto.productId);

    const existing = await this.compareItemRepository.findOne({
      where: { compareKey: normalizedKey, productId: dto.productId },
    });

    if (existing) {
      return this.getList(normalizedKey);
    }

    const currentItems = await this.compareItemRepository.find({
      where: { compareKey: normalizedKey },
      order: { sortOrder: 'ASC', createdAt: 'ASC' },
    });

    if (currentItems.length >= CompareService.MAX_COMPARE_ITEMS) {
      throw new BusinessException(
        'VALIDATION_FAILED',
        HttpStatus.BAD_REQUEST,
        `비교함에는 최대 ${CompareService.MAX_COMPARE_ITEMS}개까지 담을 수 있습니다.`,
      );
    }

    const sortOrder = currentItems.length === 0 ? 1 : Math.max(...currentItems.map((item) => item.sortOrder)) + 1;

    const created = this.compareItemRepository.create({
      compareKey: normalizedKey,
      productId: dto.productId,
      sortOrder,
    });

    await this.compareItemRepository.save(created);
    return this.getList(normalizedKey);
  }

  async remove(compareKey: string, productId: number) {
    const normalizedKey = this.normalizeCompareKey(compareKey);

    await this.compareItemRepository.softDelete({
      compareKey: normalizedKey,
      productId,
    });

    return this.getList(normalizedKey);
  }

  async getList(compareKey: string) {
    const normalizedKey = this.normalizeCompareKey(compareKey);
    const items = await this.compareItemRepository.find({
      where: { compareKey: normalizedKey },
      order: { sortOrder: 'ASC', createdAt: 'ASC' },
    });

    const compareList = await this.toCompareList(items);
    return { compareList };
  }

  // 비교 대상 상품의 핵심 필드 차이를 계산해 강조 정보를 만든다.
  async getDetail(compareKey: string) {
    const normalizedKey = this.normalizeCompareKey(compareKey);
    const items = await this.compareItemRepository.find({
      where: { compareKey: normalizedKey },
      order: { sortOrder: 'ASC', createdAt: 'ASC' },
    });

    const compareList = await this.toCompareList(items);

    const differenceMap = {
      price: this.isDifferent(compareList.map((item) => item.price)),
      categoryId: this.isDifferent(compareList.map((item) => item.categoryId)),
      averageRating: this.isDifferent(compareList.map((item) => item.averageRating)),
      reviewCount: this.isDifferent(compareList.map((item) => item.reviewCount)),
      sellerCount: this.isDifferent(compareList.map((item) => item.sellerCount)),
      salesCount: this.isDifferent(compareList.map((item) => item.salesCount)),
    };

    return {
      compareList,
      differences: differenceMap,
    };
  }

  private async toCompareList(items: CompareItem[]) {
    if (!items.length) {
      return [];
    }

    const productIds = items.map((item) => item.productId);
    const products = await this.productRepository.find({
      where: { id: In(productIds) },
    });

    const productMap = new Map(products.map((product) => [product.id, product]));

    return items
      .map((item) => {
        const product = productMap.get(item.productId);
        if (!product) {
          return null;
        }

        return {
          productId: product.id,
          name: product.name,
          categoryId: product.categoryId,
          price: product.lowestPrice ?? product.discountPrice ?? product.price,
          averageRating: Number(product.averageRating ?? 0),
          reviewCount: product.reviewCount,
          sellerCount: product.sellerCount,
          salesCount: product.salesCount,
          thumbnailUrl: product.thumbnailUrl,
        };
      })
      .filter((item): item is NonNullable<typeof item> => item !== null);
  }

  private async ensureProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
  }

  private normalizeCompareKey(compareKey?: string) {
    const key = (compareKey ?? '').trim();
    if (!key) {
      return 'guest';
    }
    return key.slice(0, 100);
  }

  private isDifferent(values: Array<string | number | null | undefined>) {
    const normalized = values.filter((value) => value !== null && value !== undefined);
    if (normalized.length <= 1) return false;
    return new Set(normalized).size > 1;
  }
}
