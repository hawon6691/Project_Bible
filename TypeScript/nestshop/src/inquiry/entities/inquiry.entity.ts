import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Product } from '../../product/entities/product.entity';
import { User } from '../../user/entities/user.entity';

export enum InquiryStatus {
  PENDING = 'PENDING',
  ANSWERED = 'ANSWERED',
}

@Entity('inquiries')
export class Inquiry extends BaseEntity {
  @Index('idx_inquiries_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_inquiries_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ type: 'varchar', length: 120 })
  title: string;

  @Column({ type: 'text' })
  content: string;

  @Column({ name: 'is_secret', type: 'boolean', default: false })
  isSecret: boolean;

  @Index('idx_inquiries_status')
  @Column({ type: 'enum', enum: InquiryStatus, default: InquiryStatus.PENDING })
  status: InquiryStatus;

  @Column({ name: 'answer_content', type: 'text', nullable: true })
  answerContent: string | null;

  @Column({ name: 'answered_by', type: 'int', nullable: true })
  answeredBy: number | null;

  @Column({ name: 'answered_at', type: 'timestamp', nullable: true })
  answeredAt: Date | null;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;
}
