import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { Seller } from '../seller/entities/seller.entity';
import { RecalculateTrustScoreDto } from './dto/recalculate-trust-score.dto';
import { TrustHistoryQueryDto } from './dto/trust-history-query.dto';
import { TrustScoreHistory } from './entities/trust-score-history.entity';

@Injectable()
export class TrustService {
  constructor(
    @InjectRepository(Seller)
    private sellerRepository: Repository<Seller>,
    @InjectRepository(TrustScoreHistory)
    private trustScoreHistoryRepository: Repository<TrustScoreHistory>,
  ) {}

  async getHistory(sellerId: number, query: TrustHistoryQueryDto) {
    await this.ensureSeller(sellerId);

    const items = await this.trustScoreHistoryRepository.find({
      where: { sellerId },
      order: { createdAt: 'DESC' },
      take: query.limit,
    });

    return items.map((item) => this.toHistoryDetail(item));
  }

  async getCurrentScore(sellerId: number) {
    const seller = await this.ensureSeller(sellerId);

    return {
      sellerId: seller.id,
      sellerName: seller.name,
      trustScore: seller.trustScore,
      trustGrade: seller.trustGrade,
      updatedAt: seller.updatedAt,
    };
  }

  async recalculateScore(sellerId: number, dto: RecalculateTrustScoreDto) {
    const seller = await this.ensureSeller(sellerId);

    const score =
      dto.deliveryAccuracy * 0.25 +
      dto.priceAccuracy * 0.25 +
      dto.customerRating * 0.2 +
      dto.responseSpeed * 0.15 +
      (100 - dto.returnRate) * 0.15;

    const trustScore = Math.max(0, Math.min(100, Math.round(score)));
    const trustGrade = this.toGrade(trustScore);

    seller.trustScore = trustScore;
    seller.trustGrade = trustGrade;
    await this.sellerRepository.save(seller);

    const history = this.trustScoreHistoryRepository.create({
      sellerId,
      deliveryAccuracy: dto.deliveryAccuracy,
      priceAccuracy: dto.priceAccuracy,
      customerRating: dto.customerRating,
      responseSpeed: dto.responseSpeed,
      returnRate: dto.returnRate,
      trustScore,
      trustGrade,
    });

    const saved = await this.trustScoreHistoryRepository.save(history);

    return {
      sellerId: seller.id,
      sellerName: seller.name,
      trustScore,
      trustGrade,
      history: this.toHistoryDetail(saved),
    };
  }

  private async ensureSeller(sellerId: number) {
    const seller = await this.sellerRepository.findOne({ where: { id: sellerId } });
    if (!seller) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return seller;
  }

  private toGrade(score: number) {
    if (score >= 90) return 'S';
    if (score >= 80) return 'A';
    if (score >= 70) return 'B';
    if (score >= 60) return 'C';
    return 'D';
  }

  private toHistoryDetail(item: TrustScoreHistory) {
    return {
      id: item.id,
      sellerId: item.sellerId,
      metrics: {
        deliveryAccuracy: item.deliveryAccuracy,
        priceAccuracy: item.priceAccuracy,
        customerRating: item.customerRating,
        responseSpeed: item.responseSpeed,
        returnRate: item.returnRate,
      },
      trustScore: item.trustScore,
      trustGrade: item.trustGrade,
      createdAt: item.createdAt,
    };
  }
}
