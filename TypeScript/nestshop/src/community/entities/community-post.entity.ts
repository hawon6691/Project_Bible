import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';

export enum CommunityBoardType {
  REVIEW = 'REVIEW',
  QNA = 'QNA',
  FREE = 'FREE',
}

@Entity('community_posts')
export class CommunityPost extends BaseEntity {
  @Index('idx_community_posts_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_community_posts_board')
  @Column({ name: 'board_type', type: 'enum', enum: CommunityBoardType })
  boardType: CommunityBoardType;

  @Column({ type: 'varchar', length: 120 })
  title: string;

  @Column({ type: 'text' })
  content: string;

  @Index('idx_community_posts_view_count')
  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;
}
