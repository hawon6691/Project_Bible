import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('search_logs')
export class SearchLog extends BaseEntity {
  @Index('idx_search_logs_user_id')
  @Column({ name: 'user_id', type: 'int', nullable: true })
  userId: number | null;

  @Index('idx_search_logs_keyword')
  @Column({ type: 'varchar', length: 100 })
  keyword: string;

  @Column({ name: 'result_count', type: 'int', default: 0 })
  resultCount: number;

  @Column({ name: 'clicked_product_id', type: 'int', nullable: true })
  clickedProductId: number | null;

  @Column({ type: 'jsonb', nullable: true })
  filters: Record<string, unknown> | null;
}
