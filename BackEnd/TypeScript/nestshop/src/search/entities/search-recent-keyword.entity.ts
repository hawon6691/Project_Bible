import { Column, Entity, Index, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('search_recent_keywords')
@Unique('uq_search_recent_user_keyword', ['userId', 'keyword'])
export class SearchRecentKeyword extends BaseEntity {
  @Index('idx_search_recent_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ type: 'varchar', length: 100 })
  keyword: string;

  @Column({ name: 'last_searched_at', type: 'timestamp' })
  lastSearchedAt: Date;
}
