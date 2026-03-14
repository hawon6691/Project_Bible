import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('faqs')
export class Faq extends BaseEntity {
  @Index('idx_faqs_category')
  @Column({ type: 'varchar', length: 50 })
  category: string;

  @Column({ type: 'varchar', length: 200 })
  question: string;

  @Column({ type: 'text' })
  answer: string;

  @Index('idx_faqs_is_active')
  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;
}
