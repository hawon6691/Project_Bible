import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('translations')
@Index('uq_translations_locale_namespace_key', ['locale', 'namespace', 'key'], { unique: true })
export class Translation extends BaseEntity {
  @Column({ type: 'varchar', length: 10 })
  locale: string;

  @Column({ type: 'varchar', length: 50 })
  namespace: string;

  @Column({ type: 'varchar', length: 120 })
  key: string;

  @Column({ type: 'text' })
  value: string;
}
