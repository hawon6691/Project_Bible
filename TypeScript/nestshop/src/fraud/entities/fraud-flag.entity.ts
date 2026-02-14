import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum FraudFlagSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

@Entity('fraud_flags')
export class FraudFlag extends BaseEntity {
  @Index('idx_fraud_flags_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Index('idx_fraud_flags_price_entry_id')
  @Column({ name: 'price_entry_id', type: 'int' })
  priceEntryId: number;

  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ type: 'varchar', length: 100 })
  reason: string;

  @Column({ name: 'raw_price', type: 'int' })
  rawPrice: number;

  @Column({ name: 'effective_price', type: 'int' })
  effectivePrice: number;

  @Column({ name: 'baseline_average', type: 'int' })
  baselineAverage: number;

  @Column({ type: 'enum', enum: FraudFlagSeverity, default: FraudFlagSeverity.LOW })
  severity: FraudFlagSeverity;
}
