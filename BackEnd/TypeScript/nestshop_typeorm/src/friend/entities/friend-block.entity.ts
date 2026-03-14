import { Column, Entity, Index, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('friend_blocks')
@Unique('uq_friend_blocks_user_blocked_user', ['userId', 'blockedUserId'])
export class FriendBlock extends BaseEntity {
  @Index('idx_friend_blocks_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_friend_blocks_blocked_user_id')
  @Column({ name: 'blocked_user_id', type: 'int' })
  blockedUserId: number;
}
