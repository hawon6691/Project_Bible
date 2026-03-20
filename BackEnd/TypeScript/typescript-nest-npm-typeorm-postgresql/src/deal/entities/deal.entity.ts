import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('deals')
export class Deal extends BaseEntity {
  @Index('idx_deals_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ type: 'varchar', length: 120 })
  title: string;

  @Column({ type: 'text', nullable: true })
  description: string | null;

  @Column({ name: 'discount_rate', type: 'int', default: 0 })
  discountRate: number;

  @Index('idx_deals_start_at')
  @Column({ name: 'start_at', type: 'timestamp' })
  startAt: Date;

  @Index('idx_deals_end_at')
  @Column({ name: 'end_at', type: 'timestamp' })
  endAt: Date;

  @Index('idx_deals_is_active')
  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;
}
