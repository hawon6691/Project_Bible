import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('system_settings')
export class SystemSetting extends BaseEntity {
  @Index('idx_system_settings_setting_key', { unique: true })
  @Column({ name: 'setting_key', type: 'varchar', length: 100, unique: true })
  settingKey: string;

  @Column({ name: 'setting_value', type: 'jsonb' })
  settingValue: Record<string, unknown>;
}
