import { Column, Entity, Index, OneToMany } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { UserBadge } from './user-badge.entity';

export enum BadgeType {
  AUTO = 'AUTO',
  MANUAL = 'MANUAL',
}

export enum BadgeRarity {
  COMMON = 'COMMON',
  UNCOMMON = 'UNCOMMON',
  RARE = 'RARE',
  EPIC = 'EPIC',
  LEGENDARY = 'LEGENDARY',
}

@Entity('badges')
export class Badge extends BaseEntity {
  @Index('idx_badges_name', { unique: true })
  @Column({ type: 'varchar', length: 100, unique: true })
  name: string;

  @Column({ type: 'varchar', length: 255 })
  description: string;

  @Column({ name: 'icon_url', type: 'varchar', length: 500 })
  iconUrl: string;

  @Column({ type: 'enum', enum: BadgeType })
  type: BadgeType;

  @Column({ type: 'jsonb', nullable: true })
  condition: Record<string, unknown> | null;

  @Column({ type: 'enum', enum: BadgeRarity, default: BadgeRarity.COMMON })
  rarity: BadgeRarity;

  @OneToMany(() => UserBadge, (userBadge) => userBadge.badge)
  userBadges: UserBadge[];
}
