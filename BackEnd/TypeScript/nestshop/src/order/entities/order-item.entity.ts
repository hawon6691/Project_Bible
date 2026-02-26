import {
  Entity, Column, ManyToOne, JoinColumn, PrimaryGeneratedColumn,
  CreateDateColumn, Index,
} from 'typeorm';
import { Order } from './order.entity';
import { Product } from '../../product/entities/product.entity';
import { Seller } from '../../seller/entities/seller.entity';

@Entity('order_items')
export class OrderItem {
  @PrimaryGeneratedColumn()
  id: number;

  @Index('idx_order_items_order')
  @Column({ name: 'order_id', type: 'int' })
  orderId: number;

  @Index('idx_order_items_product')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ name: 'product_name', type: 'varchar', length: 200 })
  productName: string;

  @Column({ name: 'seller_name', type: 'varchar', length: 100 })
  sellerName: string;

  @Column({ name: 'selected_options', type: 'varchar', length: 200, nullable: true })
  selectedOptions: string | null;

  @Column({ type: 'int' })
  quantity: number;

  @Column({ name: 'unit_price', type: 'int' })
  unitPrice: number;

  @Column({ name: 'total_price', type: 'int' })
  totalPrice: number;

  @Column({ name: 'is_reviewed', type: 'boolean', default: false })
  isReviewed: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ManyToOne(() => Order, (order) => order.items, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'order_id' })
  order: Order;

  @ManyToOne(() => Product, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'product_id' })
  product: Product;

  @ManyToOne(() => Seller, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'seller_id' })
  seller: Seller;
}
