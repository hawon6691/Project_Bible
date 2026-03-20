import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum CrawlerRunStatus {
  QUEUED = 'QUEUED',
  PROCESSING = 'PROCESSING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
}

export enum CrawlerTriggerType {
  SCHEDULED = 'SCHEDULED',
  MANUAL = 'MANUAL',
}

@Entity('crawler_runs')
export class CrawlerRun extends BaseEntity {
  @Index('idx_crawler_runs_job_id')
  @Column({ name: 'job_id', type: 'int', nullable: true })
  jobId: number | null;

  @Index('idx_crawler_runs_seller_id')
  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ name: 'product_id', type: 'int', nullable: true })
  productId: number | null;

  @Column({ name: 'trigger_type', type: 'enum', enum: CrawlerTriggerType })
  triggerType: CrawlerTriggerType;

  @Column({ name: 'collect_price', type: 'boolean', default: true })
  collectPrice: boolean;

  @Column({ name: 'collect_spec', type: 'boolean', default: true })
  collectSpec: boolean;

  @Column({ name: 'detect_anomaly', type: 'boolean', default: true })
  detectAnomaly: boolean;

  @Column({ type: 'enum', enum: CrawlerRunStatus })
  status: CrawlerRunStatus;

  @Column({ name: 'started_at', type: 'timestamp' })
  startedAt: Date;

  @Column({ name: 'ended_at', type: 'timestamp' })
  endedAt: Date;

  @Column({ name: 'duration_ms', type: 'int' })
  durationMs: number;

  @Column({ name: 'collected_price_count', type: 'int', default: 0 })
  collectedPriceCount: number;

  @Column({ name: 'collected_spec_count', type: 'int', default: 0 })
  collectedSpecCount: number;

  @Column({ name: 'anomaly_count', type: 'int', default: 0 })
  anomalyCount: number;

  @Column({ name: 'error_message', type: 'varchar', length: 500, nullable: true })
  errorMessage: string | null;
}
