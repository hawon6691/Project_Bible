import { Column, Entity, Index, JoinColumn, ManyToOne, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';

export enum SocialProvider {
  GOOGLE = 'google',
  NAVER = 'naver',
  KAKAO = 'kakao',
  FACEBOOK = 'facebook',
  INSTAGRAM = 'instagram',
}

@Entity('social_accounts')
@Unique('uq_social_accounts_provider_user', ['provider', 'providerUserId'])
export class SocialAccount extends BaseEntity {
  @Index('idx_social_accounts_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_social_accounts_provider')
  @Column({ type: 'enum', enum: SocialProvider })
  provider: SocialProvider;

  @Column({ name: 'provider_user_id', type: 'varchar', length: 200 })
  providerUserId: string;

  @Column({ name: 'provider_email', type: 'varchar', length: 255, nullable: true })
  providerEmail: string | null;

  @Column({ name: 'provider_name', type: 'varchar', length: 100, nullable: true })
  providerName: string | null;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;
}
