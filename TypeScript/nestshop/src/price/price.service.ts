import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThanOrEqual, MoreThanOrEqual } from 'typeorm';
import { CACHE_KEYS, CACHE_KEY_PREFIX, CACHE_TTL_SECONDS } from '../common/cache/cache-policy.constants';
import { CacheService } from '../common/cache/cache.service';
import { PriceEntry } from './entities/price-entry.entity';
import { PriceHistory } from './entities/price-history.entity';
import { PriceAlert } from './entities/price-alert.entity';
import { Product } from '../product/entities/product.entity';
import { CreatePriceEntryDto, UpdatePriceEntryDto, CreatePriceAlertDto } from './dto/create-price-entry.dto';
import { BusinessException } from '../common/exceptions/business.exception';

@Injectable()
export class PriceService {
  constructor(
    @InjectRepository(PriceEntry)
    private priceEntryRepository: Repository<PriceEntry>,
    @InjectRepository(PriceHistory)
    private priceHistoryRepository: Repository<PriceHistory>,
    @InjectRepository(PriceAlert)
    private priceAlertRepository: Repository<PriceAlert>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    private readonly cacheService: CacheService,
  ) {}

  // ─── PRICE-01, SELL-05: 판매처별 가격비교 조회 ───
  async getProductPrices(productId: number) {
    const cacheKey = CACHE_KEYS.priceCompare(productId);
    const cached = await this.cacheService.getJson<any>(cacheKey);
    if (cached) {
      return cached;
    }

    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const entries = await this.priceEntryRepository.find({
      where: { productId, isAvailable: true },
      relations: ['seller'],
      order: { price: 'ASC' },
    });

    const prices = entries.map((e) => e.price);
    const lowestPrice = prices.length > 0 ? Math.min(...prices) : null;
    const highestPrice = prices.length > 0 ? Math.max(...prices) : null;
    const averagePrice = prices.length > 0 ? Math.round(prices.reduce((a, b) => a + b, 0) / prices.length) : null;

    const result = {
      lowestPrice,
      averagePrice,
      highestPrice,
      entries: entries.map((e) => ({
        id: e.id,
        seller: {
          id: e.seller.id,
          name: e.seller.name,
          logoUrl: e.seller.logoUrl,
          trustScore: e.seller.trustScore,
        },
        price: e.price,
        shippingCost: e.shippingCost,
        shippingInfo: e.shippingInfo,
        productUrl: e.productUrl,
        updatedAt: e.updatedAt,
      })),
    };
    await this.cacheService.setJson(cacheKey, result, CACHE_TTL_SECONDS.PRICE_COMPARE);
    return result;
  }

  // ─── SELL-04: 판매처 가격 등록 ───
  async createPriceEntry(productId: number, dto: CreatePriceEntryDto) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const entry = this.priceEntryRepository.create({
      productId,
      sellerId: dto.sellerId,
      price: dto.price,
      shippingCost: dto.shippingCost || 0,
      shippingInfo: dto.shippingInfo || null,
      productUrl: dto.productUrl,
      shippingFee: dto.shippingCost || 0,
      shippingType: dto.shippingType,
    });

    const saved = await this.priceEntryRepository.save(entry);

    // 상품 최저가/판매처수 비정규화 갱신
    await this.updateProductPriceStats(productId);
    await this.invalidatePriceCache(productId);

    return saved;
  }

  // ─── 가격 수정 ───
  async updatePriceEntry(id: number, dto: UpdatePriceEntryDto) {
    const entry = await this.priceEntryRepository.findOne({ where: { id } });
    if (!entry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.price !== undefined) entry.price = dto.price;
    if (dto.shippingCost !== undefined) { entry.shippingCost = dto.shippingCost; entry.shippingFee = dto.shippingCost; }
    if (dto.shippingInfo !== undefined) entry.shippingInfo = dto.shippingInfo;
    if (dto.productUrl !== undefined) entry.productUrl = dto.productUrl;
    if (dto.isAvailable !== undefined) entry.isAvailable = dto.isAvailable;

    const saved = await this.priceEntryRepository.save(entry);
    await this.updateProductPriceStats(entry.productId);
    await this.invalidatePriceCache(entry.productId);
    return saved;
  }

  // ─── 가격 삭제 ───
  async removePriceEntry(id: number) {
    const entry = await this.priceEntryRepository.findOne({ where: { id } });
    if (!entry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    const productId = entry.productId;
    await this.priceEntryRepository.remove(entry);
    await this.updateProductPriceStats(productId);
    await this.invalidatePriceCache(productId);
    return { message: '가격이 삭제되었습니다.' };
  }

  // ─── PRICE-02: 가격 추이 조회 ───
  async getPriceHistory(productId: number, period: string = '3m') {
    const cacheKey = CACHE_KEYS.priceHistory(productId, period);
    const cached = await this.cacheService.getJson<any>(cacheKey);
    if (cached) {
      return cached;
    }

    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const startDate = this.calculateStartDate(period);

    const history = await this.priceHistoryRepository.find({
      where: {
        productId,
        date: MoreThanOrEqual(startDate),
      },
      order: { date: 'DESC' },
    });

    // 전체 최저/최고가
    const allHistory = await this.priceHistoryRepository.find({ where: { productId } });
    const allLowest = allHistory.length > 0 ? Math.min(...allHistory.map((h) => h.lowestPrice)) : null;
    const allHighest = allHistory.length > 0 ? Math.max(...allHistory.map((h) => h.highestPrice)) : null;

    const result = {
      productId,
      productName: product.name,
      allTimeLowest: allLowest,
      allTimeHighest: allHighest,
      history: history.map((h) => ({
        date: h.date,
        lowestPrice: h.lowestPrice,
        averagePrice: h.averagePrice,
      })),
    };
    await this.cacheService.setJson(cacheKey, result, CACHE_TTL_SECONDS.PRICE_HISTORY);
    return result;
  }

  // ─── PRICE-03: 최저가 알림 등록 ───
  async createAlert(userId: number, dto: CreatePriceAlertDto) {
    const product = await this.productRepository.findOne({ where: { id: dto.productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const existing = await this.priceAlertRepository.findOne({
      where: { userId, productId: dto.productId },
    });
    if (existing) {
      throw new BusinessException('ALERT_ALREADY_EXISTS', HttpStatus.CONFLICT);
    }

    const alert = this.priceAlertRepository.create({
      userId,
      productId: dto.productId,
      targetPrice: dto.targetPrice,
    });

    const saved = await this.priceAlertRepository.save(alert);
    return {
      id: saved.id,
      productId: saved.productId,
      productName: product.name,
      targetPrice: saved.targetPrice,
      currentLowestPrice: product.lowestPrice,
      isTriggered: saved.isTriggered,
      createdAt: saved.createdAt,
    };
  }

  // ─── PRICE-04: 내 알림 목록 ───
  async getMyAlerts(userId: number) {
    const alerts = await this.priceAlertRepository.find({
      where: { userId, isActive: true },
      relations: ['product'],
      order: { createdAt: 'DESC' },
    });

    return alerts.map((a) => ({
      id: a.id,
      productId: a.productId,
      productName: a.product?.name,
      targetPrice: a.targetPrice,
      currentLowestPrice: a.product?.lowestPrice,
      isTriggered: a.isTriggered,
      triggeredAt: a.triggeredAt,
      createdAt: a.createdAt,
    }));
  }

  // ─── PRICE-05: 알림 삭제 ───
  async removeAlert(userId: number, alertId: number) {
    const alert = await this.priceAlertRepository.findOne({
      where: { id: alertId, userId },
    });
    if (!alert) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.priceAlertRepository.remove(alert);
    return { message: '알림이 삭제되었습니다.' };
  }

  // ─── 헬퍼: 상품 가격 통계 갱신 ───
  private async updateProductPriceStats(productId: number) {
    const entries = await this.priceEntryRepository.find({
      where: { productId, isAvailable: true },
    });

    const lowestPrice = entries.length > 0 ? Math.min(...entries.map((e) => e.price)) : null;
    const sellerCount = entries.length;

    await this.productRepository.update(productId, { lowestPrice, sellerCount });
  }

  // ─── 헬퍼: 기간 계산 ───
  private calculateStartDate(period: string): string {
    const now = new Date();
    switch (period) {
      case '1w': now.setDate(now.getDate() - 7); break;
      case '1m': now.setMonth(now.getMonth() - 1); break;
      case '6m': now.setMonth(now.getMonth() - 6); break;
      case '1y': now.setFullYear(now.getFullYear() - 1); break;
      case '3m': default: now.setMonth(now.getMonth() - 3); break;
    }
    return now.toISOString().split('T')[0];
  }

  private async invalidatePriceCache(productId: number) {
    await this.cacheService.del(CACHE_KEYS.priceCompare(productId));
    await this.cacheService.delByPattern(`${CACHE_KEY_PREFIX.PRICE}:history:${productId}:*`);
  }
}
