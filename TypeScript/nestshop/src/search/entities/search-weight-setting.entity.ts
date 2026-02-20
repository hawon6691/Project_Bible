import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('search_weight_settings')
export class SearchWeightSetting extends BaseEntity {
  @Index('idx_search_weight_settings_name', { unique: true })
  @Column({ name: 'setting_name', type: 'varchar', length: 50, unique: true })
  settingName: string;

  @Column({ type: 'jsonb' })
  weights: Record<string, number>;
}
