import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('push_subscriptions')
export class PushSubscription extends BaseEntity {
  @Index('idx_push_subscriptions_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ type: 'varchar', length: 1000 })
  endpoint: string;

  @Column({ name: 'p256dh_key', type: 'varchar', length: 255 })
  p256dhKey: string;

  @Column({ name: 'auth_key', type: 'varchar', length: 255 })
  authKey: string;

  @Column({ name: 'expiration_time', type: 'bigint', nullable: true })
  expirationTime: string | null;

  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;
}
