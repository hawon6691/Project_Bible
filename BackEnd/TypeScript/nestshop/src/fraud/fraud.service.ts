import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceEntry, ShippingType } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { FraudAlertQueryDto } from './dto/fraud-alert-query.dto';
import { FraudScanQueryDto } from './dto/fraud-scan-query.dto';
import { RealPriceQueryDto } from './dto/real-price-query.dto';
import { FraudFlag, FraudFlagSeverity, FraudFlagStatus } from './entities/fraud-flag.entity';

@Injectable()
export class FraudService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(PriceEntry)
    private priceEntryRepository: Repository<PriceEntry>,
    @InjectRepository(FraudFlag)
    private fraudFlagRepository: Repository<FraudFlag>,
  ) {}

  // FRAUD-02: 배송비 포함 체감가 계산
  async getEffectivePrices(productId: number) {
    await this.ensureProduct(productId);

    const entries = await this.priceEntryRepository.find({
      where: { productId, isAvailable: true },
      relations: ['seller'],
      order: { price: 'ASC' },
    });

    return entries.map((entry) => ({
      priceEntryId: entry.id,
      sellerId: entry.sellerId,
      sellerName: entry.seller?.name,
      productPrice: entry.price,
      shippingFee: entry.shippingFee,
      shippingType: entry.shippingType,
      effectivePrice: this.calculateEffectivePrice(entry),
      shippingInfo: entry.shippingInfo,
      productUrl: entry.productUrl,
    }));
  }

  // FRAUD-01: 이상 가격 탐지 (평균 대비 비정상 값)
  async detectAnomalies(productId: number, query: FraudScanQueryDto, persist = false) {
    await this.ensureProduct(productId);

    const entries = await this.priceEntryRepository.find({
      where: { productId, isAvailable: true },
      relations: ['seller'],
      take: query.limit,
    });

    if (entries.length === 0) {
      return {
        productId,
        baselineAverage: 0,
        scannedCount: 0,
        anomalies: [],
      };
    }

    const normalized = entries.map((entry) => ({
      entry,
      effectivePrice: this.calculateEffectivePrice(entry),
    }));

    const avg = Math.round(normalized.reduce((acc, cur) => acc + cur.effectivePrice, 0) / normalized.length);
    const lowerBound = Math.round(avg * (query.lowerBoundRatio ?? 0.5));
    const upperBound = Math.round(avg * (query.upperBoundRatio ?? 1.8));

    const anomalies = normalized
      .filter((item) => item.effectivePrice <= lowerBound || item.effectivePrice >= upperBound)
      .map((item) => {
        const gap = Math.abs(item.effectivePrice - avg) / Math.max(avg, 1);
        const severity =
          gap >= 0.7 ? FraudFlagSeverity.HIGH : gap >= 0.4 ? FraudFlagSeverity.MEDIUM : FraudFlagSeverity.LOW;

        return {
          priceEntryId: item.entry.id,
          sellerId: item.entry.sellerId,
          sellerName: item.entry.seller?.name,
          rawPrice: item.entry.price,
          effectivePrice: item.effectivePrice,
          baselineAverage: avg,
          lowerBound,
          upperBound,
          severity,
          reason: item.effectivePrice <= lowerBound ? '평균 대비 과도한 저가' : '평균 대비 과도한 고가',
        };
      });

    if (persist && anomalies.length > 0) {
      const flags = anomalies.map((item) =>
        this.fraudFlagRepository.create({
          productId,
          priceEntryId: item.priceEntryId,
          sellerId: item.sellerId,
          reason: item.reason,
          rawPrice: item.rawPrice,
          effectivePrice: item.effectivePrice,
          baselineAverage: item.baselineAverage,
          severity: item.severity,
          status: FraudFlagStatus.PENDING,
          reviewedBy: null,
          reviewedAt: null,
        }),
      );
      await this.fraudFlagRepository.save(flags);
    }

    return {
      productId,
      baselineAverage: avg,
      lowerBound,
      upperBound,
      scannedCount: normalized.length,
      anomalyCount: anomalies.length,
      anomalies,
    };
  }

  async getFlags(productId: number, limit = 50) {
    await this.ensureProduct(productId);

    const items = await this.fraudFlagRepository.find({
      where: { productId },
      order: { createdAt: 'DESC' },
      take: limit,
    });

    return items.map((item) => ({
      id: item.id,
      priceEntryId: item.priceEntryId,
      sellerId: item.sellerId,
      reason: item.reason,
      rawPrice: item.rawPrice,
      effectivePrice: item.effectivePrice,
      baselineAverage: item.baselineAverage,
      severity: item.severity,
      status: item.status,
      createdAt: item.createdAt,
    }));
  }

  async getAlerts(query: FraudAlertQueryDto) {
    const [items, total] = await this.fraudFlagRepository.findAndCount({
      where: query.status ? { status: query.status } : {},
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const mapped = items.map((item) => ({
      id: item.id,
      productId: item.productId,
      priceEntryId: item.priceEntryId,
      sellerId: item.sellerId,
      reason: item.reason,
      rawPrice: item.rawPrice,
      effectivePrice: item.effectivePrice,
      baselineAverage: item.baselineAverage,
      severity: item.severity,
      status: item.status,
      reviewedBy: item.reviewedBy,
      reviewedAt: item.reviewedAt,
      createdAt: item.createdAt,
    }));

    return new PaginationResponseDto(mapped, total, query.page, query.limit);
  }

  async approveAlert(alertId: number, adminUserId: number) {
    const alert = await this.ensureAlert(alertId);
    alert.status = FraudFlagStatus.APPROVED;
    alert.reviewedBy = adminUserId;
    alert.reviewedAt = new Date();
    await this.fraudFlagRepository.save(alert);

    return { success: true, message: '이상 가격 알림을 승인했습니다.' };
  }

  // 거절된 이상 가격 데이터는 노출 제외를 위해 해당 가격 엔트리를 비활성화한다.
  async rejectAlert(alertId: number, adminUserId: number) {
    const alert = await this.ensureAlert(alertId);
    alert.status = FraudFlagStatus.REJECTED;
    alert.reviewedBy = adminUserId;
    alert.reviewedAt = new Date();
    await this.fraudFlagRepository.save(alert);

    await this.priceEntryRepository.update({ id: alert.priceEntryId }, { isAvailable: false });
    return { success: true, message: '이상 가격 알림을 거절했습니다.' };
  }

  async getRealPrice(productId: number, query: RealPriceQueryDto) {
    await this.ensureProduct(productId);

    const where = query.sellerId
      ? { productId, sellerId: query.sellerId, isAvailable: true }
      : { productId, isAvailable: true };

    const entry = await this.priceEntryRepository.findOne({
      where,
      order: { price: 'ASC' },
    });

    if (!entry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return {
      productId,
      sellerId: entry.sellerId,
      productPrice: entry.price,
      shippingFee: entry.shippingFee,
      shippingType: entry.shippingType,
      totalPrice: this.calculateEffectivePrice(entry),
    };
  }

  private calculateEffectivePrice(entry: PriceEntry) {
    if (entry.shippingType === ShippingType.FREE) {
      return entry.price;
    }

    // CONDITIONAL 타입은 임계값 정보가 없으므로 보수적으로 배송비를 포함한다.
    return entry.price + (entry.shippingFee ?? 0);
  }

  private async ensureProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
  }

  private async ensureAlert(alertId: number) {
    const alert = await this.fraudFlagRepository.findOne({ where: { id: alertId } });
    if (!alert) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return alert;
  }
}
