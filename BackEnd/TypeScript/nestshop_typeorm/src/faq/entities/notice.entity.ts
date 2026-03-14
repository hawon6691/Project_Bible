import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('notices')
export class Notice extends BaseEntity {
  @Column({ type: 'varchar', length: 200 })
  title: string;

  @Column({ type: 'text' })
  content: string;

  @Index('idx_notices_is_published')
  @Column({ name: 'is_published', type: 'boolean', default: true })
  isPublished: boolean;
}
