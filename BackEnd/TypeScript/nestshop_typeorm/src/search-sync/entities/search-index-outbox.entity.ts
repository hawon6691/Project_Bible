import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum SearchIndexOutboxEventType {
  PRODUCT_UPSERT = 'PRODUCT_UPSERT',
  PRODUCT_DELETE = 'PRODUCT_DELETE',
  PRICE_CHANGED = 'PRICE_CHANGED',
}

export enum SearchIndexOutboxStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

@Entity('search_index_outbox')
export class SearchIndexOutbox extends BaseEntity {
  @Index('idx_search_index_outbox_event_type')
  @Column({ name: 'event_type', type: 'enum', enum: SearchIndexOutboxEventType })
  eventType: SearchIndexOutboxEventType;

  @Index('idx_search_index_outbox_status')
  @Column({ type: 'enum', enum: SearchIndexOutboxStatus, default: SearchIndexOutboxStatus.PENDING })
  status: SearchIndexOutboxStatus;

  @Column({ name: 'aggregate_id', type: 'int' })
  aggregateId: number;

  @Column({ type: 'json', nullable: true })
  payload: Record<string, unknown> | null;

  @Column({ name: 'attempt_count', type: 'int', default: 0 })
  attemptCount: number;

  @Column({ name: 'last_error', type: 'varchar', length: 500, nullable: true })
  lastError: string | null;

  @Column({ name: 'processed_at', type: 'timestamp', nullable: true })
  processedAt: Date | null;
}
