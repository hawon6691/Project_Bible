import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';
import { Badge } from './badge.entity';

@Entity('user_badges')
@Index('idx_user_badges_user_id', ['userId'])
@Index('idx_user_badges_badge_id', ['badgeId'])
export class UserBadge extends BaseEntity {
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ name: 'badge_id', type: 'int' })
  badgeId: number;

  @Column({ name: 'granted_by_admin_id', type: 'int', nullable: true })
  grantedByAdminId: number | null;

  @Column({ type: 'varchar', length: 255, nullable: true })
  reason: string | null;

  @Column({ name: 'granted_at', type: 'timestamp' })
  grantedAt: Date;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Badge, (badge) => badge.userBadges, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'badge_id' })
  badge: Badge;
}
