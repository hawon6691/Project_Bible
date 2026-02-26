import {
  Entity, Column, ManyToOne, JoinColumn, PrimaryGeneratedColumn,
  CreateDateColumn, UpdateDateColumn, Index, Unique,
} from 'typeorm';
import { Product } from '../../product/entities/product.entity';
import { Seller } from '../../seller/entities/seller.entity';

export enum ShippingType {
  FREE = 'FREE',
  PAID = 'PAID',
  CONDITIONAL = 'CONDITIONAL',
}

@Entity('price_entries')
@Unique('uq_price_entries', ['productId', 'sellerId'])
export class PriceEntry {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ type: 'int' })
  price: number;

  @Column({ name: 'shipping_cost', type: 'int', default: 0 })
  shippingCost: number;

  @Column({ name: 'shipping_info', type: 'varchar', length: 100, nullable: true })
  shippingInfo: string | null;

  @Column({ name: 'product_url', type: 'varchar', length: 1000 })
  productUrl: string;

  @Column({ name: 'shipping_fee', type: 'int', default: 0 })
  shippingFee: number;

  @Column({ name: 'shipping_type', type: 'enum', enum: ShippingType, default: ShippingType.PAID })
  shippingType: ShippingType;

  @Column({ name: 'click_count', type: 'int', default: 0 })
  clickCount: number;

  @Column({ name: 'is_available', type: 'boolean', default: true })
  isAvailable: boolean;

  @Column({ name: 'crawled_at', type: 'timestamp', nullable: true })
  crawledAt: Date | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;

  @ManyToOne(() => Seller, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'seller_id' })
  seller: Seller;
}
