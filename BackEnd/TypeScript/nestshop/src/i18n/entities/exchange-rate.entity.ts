import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('exchange_rates')
@Index('uq_exchange_rates_base_target', ['baseCurrency', 'targetCurrency'], { unique: true })
export class ExchangeRate extends BaseEntity {
  @Column({ name: 'base_currency', type: 'varchar', length: 3 })
  baseCurrency: string;

  @Column({ name: 'target_currency', type: 'varchar', length: 3 })
  targetCurrency: string;

  @Column({ type: 'decimal', precision: 18, scale: 8 })
  rate: string;
}
