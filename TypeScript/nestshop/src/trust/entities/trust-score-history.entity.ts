import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('trust_score_histories')
export class TrustScoreHistory extends BaseEntity {
  @Index('idx_trust_histories_seller_id')
  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ name: 'delivery_accuracy', type: 'decimal', precision: 5, scale: 2, default: 0 })
  deliveryAccuracy: number;

  @Column({ name: 'price_accuracy', type: 'decimal', precision: 5, scale: 2, default: 0 })
  priceAccuracy: number;

  @Column({ name: 'customer_rating', type: 'decimal', precision: 5, scale: 2, default: 0 })
  customerRating: number;

  @Column({ name: 'response_speed', type: 'decimal', precision: 5, scale: 2, default: 0 })
  responseSpeed: number;

  @Column({ name: 'return_rate', type: 'decimal', precision: 5, scale: 2, default: 0 })
  returnRate: number;

  @Column({ name: 'trust_score', type: 'int' })
  trustScore: number;

  @Column({ name: 'trust_grade', type: 'varchar', length: 2 })
  trustGrade: string;
}
