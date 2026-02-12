import {
  Entity,
  Column,
  Index,
  OneToMany,
} from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { UserRole } from '../../common/decorators/roles.decorator';

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLOCKED = 'BLOCKED',
}

@Entity('users')
export class User extends BaseEntity {
  @Index('idx_users_email', { unique: true })
  @Column({ type: 'varchar', length: 255, unique: true })
  email: string;

  @Column({ type: 'varchar', length: 255, nullable: true })
  password: string | null;

  @Column({ type: 'varchar', length: 50 })
  name: string;

  @Column({ type: 'varchar', length: 20 })
  phone: string;

  @Index('idx_users_role')
  @Column({ type: 'enum', enum: UserRole, default: UserRole.USER })
  role: UserRole;

  @Index('idx_users_status')
  @Column({ type: 'enum', enum: UserStatus, default: UserStatus.ACTIVE })
  status: UserStatus;

  @Column({ name: 'email_verified', type: 'boolean', default: false })
  emailVerified: boolean;

  @Column({ name: 'email_verified_at', type: 'timestamp', nullable: true })
  emailVerifiedAt: Date | null;

  @Index('idx_users_nickname', { unique: true })
  @Column({ type: 'varchar', length: 30, unique: true })
  nickname: string;

  @Column({ type: 'varchar', length: 200, nullable: true })
  bio: string | null;

  @Column({ name: 'profile_image_url', type: 'varchar', length: 500, nullable: true })
  profileImageUrl: string | null;

  @Column({ name: 'search_history_enabled', type: 'boolean', default: true })
  searchHistoryEnabled: boolean;

  @Column({ type: 'int', default: 0 })
  point: number;

  @Column({ name: 'preferred_locale', type: 'varchar', length: 5, default: 'ko' })
  preferredLocale: string;

  @Column({ name: 'preferred_currency', type: 'varchar', length: 3, default: 'KRW' })
  preferredCurrency: string;

  @Column({ name: 'refresh_token', type: 'varchar', length: 500, nullable: true })
  refreshToken: string | null;
}
