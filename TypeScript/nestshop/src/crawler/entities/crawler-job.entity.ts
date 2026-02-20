import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('crawler_jobs')
export class CrawlerJob extends BaseEntity {
  @Index('idx_crawler_jobs_seller_id')
  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ type: 'varchar', length: 100 })
  name: string;

  @Column({ name: 'cron_expression', type: 'varchar', length: 100, nullable: true })
  cronExpression: string | null;

  @Column({ name: 'collect_price', type: 'boolean', default: true })
  collectPrice: boolean;

  @Column({ name: 'collect_spec', type: 'boolean', default: true })
  collectSpec: boolean;

  @Column({ name: 'detect_anomaly', type: 'boolean', default: true })
  detectAnomaly: boolean;

  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;

  @Column({ name: 'last_triggered_at', type: 'timestamp', nullable: true })
  lastTriggeredAt: Date | null;
}
