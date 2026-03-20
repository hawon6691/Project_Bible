import {
  Entity, Column, ManyToOne, JoinColumn, PrimaryGeneratedColumn,
  CreateDateColumn, Unique, Index,
} from 'typeorm';
import { Product } from '../../product/entities/product.entity';

@Entity('price_history')
@Unique('uq_price_history', ['productId', 'date'])
export class PriceHistory {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Index('idx_price_history_date')
  @Column({ type: 'date' })
  date: string;

  @Column({ name: 'lowest_price', type: 'int' })
  lowestPrice: number;

  @Column({ name: 'average_price', type: 'int' })
  averagePrice: number;

  @Column({ name: 'highest_price', type: 'int' })
  highestPrice: number;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;
}
