import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceHistory } from '../price/entities/price-history.entity';
import { Product } from '../product/entities/product.entity';
import { PricePredictionQueryDto } from './dto/price-prediction-query.dto';

@Injectable()
export class PredictionService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(PriceHistory)
    private priceHistoryRepository: Repository<PriceHistory>,
  ) {}

  // PRICE-ANLY: 추세 기반 단순 가격 예측
  async predictProductPrice(productId: number, query: PricePredictionQueryDto) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const histories = await this.priceHistoryRepository.find({
      where: { productId },
      order: { date: 'DESC' },
      take: query.lookbackDays,
    });

    if (histories.length < 2) {
      const fallbackPrice = product.lowestPrice ?? product.discountPrice ?? product.price;
      return {
        productId,
        productName: product.name,
        basis: {
          dataPoints: histories.length,
          horizonDays: query.horizonDays,
          lookbackDays: query.lookbackDays,
        },
        trend: 'UNKNOWN',
        currentPrice: fallbackPrice,
        predictedPrice: fallbackPrice,
        expectedChange: 0,
        confidence: 0.2,
        message: '가격 이력 데이터가 부족해 현재 가격을 기준으로 반환합니다.',
      };
    }

    // 오래된 순서로 정렬해 추세 계산에 사용
    const series = [...histories].reverse();
    const y = series.map((h) => h.averagePrice);

    // 최소제곱법 선형회귀 y = a + b*x
    const n = y.length;
    const x = y.map((_, idx) => idx);
    const sumX = x.reduce((acc, v) => acc + v, 0);
    const sumY = y.reduce((acc, v) => acc + v, 0);
    const sumXY = x.reduce((acc, v, idx) => acc + v * y[idx], 0);
    const sumXX = x.reduce((acc, v) => acc + v * v, 0);

    const denominator = n * sumXX - sumX * sumX;
    const slope = denominator === 0 ? 0 : (n * sumXY - sumX * sumY) / denominator;
    const intercept = (sumY - slope * sumX) / n;

    const currentPrice = y[n - 1];
    const predictedRaw = intercept + slope * (n - 1 + query.horizonDays);
    const predictedPrice = Math.max(0, Math.round(predictedRaw));

    const expectedChange = predictedPrice - currentPrice;
    const trend = slope > 0 ? 'UP' : slope < 0 ? 'DOWN' : 'FLAT';

    // 표준편차 기반 간단 신뢰도 산출 (변동성 낮을수록 신뢰도↑)
    const mean = sumY / n;
    const variance = y.reduce((acc, v) => acc + (v - mean) ** 2, 0) / n;
    const stddev = Math.sqrt(variance);
    const volatility = mean === 0 ? 1 : stddev / mean;
    const confidence = Number(Math.max(0.2, Math.min(0.95, (n / 60) * (1 - volatility))).toFixed(2));

    return {
      productId,
      productName: product.name,
      basis: {
        dataPoints: n,
        horizonDays: query.horizonDays,
        lookbackDays: query.lookbackDays,
        fromDate: series[0].date,
        toDate: series[n - 1].date,
      },
      trend,
      currentPrice,
      predictedPrice,
      expectedChange,
      confidence,
      message: '단순 선형회귀 기반 추세 예측 결과입니다.',
    };
  }
}
