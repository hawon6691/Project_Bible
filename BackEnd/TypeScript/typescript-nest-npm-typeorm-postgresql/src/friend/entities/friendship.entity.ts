import { Column, Entity, Index, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum FriendshipStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
}

@Entity('friendships')
@Unique('uq_friendships_requester_addressee', ['requesterId', 'addresseeId'])
export class Friendship extends BaseEntity {
  @Index('idx_friendships_requester_id')
  @Column({ name: 'requester_id', type: 'int' })
  requesterId: number;

  @Index('idx_friendships_addressee_id')
  @Column({ name: 'addressee_id', type: 'int' })
  addresseeId: number;

  @Index('idx_friendships_status')
  @Column({ type: 'enum', enum: FriendshipStatus, default: FriendshipStatus.PENDING })
  status: FriendshipStatus;

  @Column({ name: 'responded_at', type: 'timestamp', nullable: true })
  respondedAt: Date | null;
}
