import {
  Entity, Column, ManyToOne, JoinColumn, PrimaryGeneratedColumn,
  CreateDateColumn, UpdateDateColumn, Index, Unique,
} from 'typeorm';
import { User } from '../../user/entities/user.entity';
import { Product } from '../../product/entities/product.entity';
import { Seller } from '../../seller/entities/seller.entity';

@Entity('cart_items')
@Unique('uq_cart_items', ['userId', 'productId', 'sellerId', 'selectedOptions'])
export class CartItem {
  @PrimaryGeneratedColumn()
  id: number;

  @Index('idx_cart_items_user')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ name: 'selected_options', type: 'varchar', length: 200, nullable: true })
  selectedOptions: string | null;

  @Column({ type: 'int', default: 1 })
  quantity: number;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;

  @ManyToOne(() => Seller, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'seller_id' })
  seller: Seller;
}
