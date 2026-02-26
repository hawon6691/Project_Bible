import {
  Entity, Column, ManyToOne, JoinColumn, PrimaryGeneratedColumn,
  CreateDateColumn, Unique, Index,
} from 'typeorm';
import { User } from '../../user/entities/user.entity';
import { Product } from '../../product/entities/product.entity';

@Entity('price_alerts')
@Unique('uq_price_alerts', ['userId', 'productId'])
export class PriceAlert {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'target_price', type: 'int' })
  targetPrice: number;

  @Column({ name: 'is_triggered', type: 'boolean', default: false })
  isTriggered: boolean;

  @Column({ name: 'triggered_at', type: 'timestamp', nullable: true })
  triggeredAt: Date | null;

  @Index('idx_price_alerts_active')
  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;
}
