import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('push_preferences')
export class PushPreference extends BaseEntity {
  @Index('idx_push_preferences_user_id', { unique: true })
  @Column({ name: 'user_id', type: 'int', unique: true })
  userId: number;

  @Column({ name: 'price_alert_enabled', type: 'boolean', default: true })
  priceAlertEnabled: boolean;

  @Column({ name: 'order_status_enabled', type: 'boolean', default: true })
  orderStatusEnabled: boolean;

  @Column({ name: 'chat_message_enabled', type: 'boolean', default: true })
  chatMessageEnabled: boolean;

  @Column({ name: 'deal_enabled', type: 'boolean', default: true })
  dealEnabled: boolean;
}
