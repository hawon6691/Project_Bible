import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceHistory } from '../price/entities/price-history.entity';
import { Product } from '../product/entities/product.entity';

@Injectable()
export class AnalyticsService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(PriceHistory)
    private priceHistoryRepository: Repository<PriceHistory>,
  ) {}

  async getLowestEver(productId: number) {
    const product = await this.ensureProduct(productId);

    const currentPrice = product.lowestPrice ?? product.discountPrice ?? product.price;

    const lowestHistory = await this.priceHistoryRepository.findOne({
      where: { productId },
      order: { lowestPrice: 'ASC', date: 'ASC' },
    });

    if (!lowestHistory) {
      return {
        isLowestEver: true,
        currentPrice,
        lowestPrice: currentPrice,
        lowestDate: null,
      };
    }

    return {
      isLowestEver: currentPrice <= lowestHistory.lowestPrice,
      currentPrice,
      lowestPrice: lowestHistory.lowestPrice,
      lowestDate: lowestHistory.date,
    };
  }

  // 상품명/설명에서 수량 단위를 추출해 단가를 계산한다.
  async getUnitPrice(productId: number) {
    const product = await this.ensureProduct(productId);
    const basePrice = product.lowestPrice ?? product.discountPrice ?? product.price;

    const parsed = this.extractQuantityUnit(`${product.name} ${product.description ?? ''}`);

    if (!parsed) {
      return {
        unitPrice: basePrice,
        unit: 'ea',
        quantity: 1,
      };
    }

    const { quantity, unit } = parsed;
    return {
      unitPrice: Number((basePrice / Math.max(quantity, 1)).toFixed(2)),
      unit,
      quantity,
    };
  }

  private extractQuantityUnit(text: string) {
    const normalized = text.toLowerCase();

    const patterns: Array<{ regex: RegExp; unit: string; factor?: number }> = [
      { regex: /(\d+(?:\.\d+)?)\s?kg\b/, unit: 'kg' },
      { regex: /(\d+(?:\.\d+)?)\s?g\b/, unit: 'g' },
      { regex: /(\d+(?:\.\d+)?)\s?l\b/, unit: 'l' },
      { regex: /(\d+(?:\.\d+)?)\s?ml\b/, unit: 'ml' },
      { regex: /(\d+(?:\.\d+)?)\s?tb\b/, unit: 'tb' },
      { regex: /(\d+(?:\.\d+)?)\s?gb\b/, unit: 'gb' },
      { regex: /(\d+)\s?(?:개|ea|pcs|pack)\b/, unit: 'ea' },
    ];

    for (const pattern of patterns) {
      const matched = normalized.match(pattern.regex);
      if (!matched) continue;

      const quantity = Number(matched[1]);
      if (!Number.isFinite(quantity) || quantity <= 0) continue;

      return { quantity, unit: pattern.unit };
    }

    return null;
  }

  private async ensureProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return product;
  }
}
