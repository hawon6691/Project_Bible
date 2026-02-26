import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';

@Entity('search_histories')
export class SearchHistory extends BaseEntity {
  @Index('idx_search_histories_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_search_histories_keyword')
  @Column({ type: 'varchar', length: 100 })
  keyword: string;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;
}
