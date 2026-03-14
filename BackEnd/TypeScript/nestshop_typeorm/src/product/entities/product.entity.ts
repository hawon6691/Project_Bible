import {
  Entity,
  Column,
  Index,
  ManyToOne,
  OneToMany,
  JoinColumn,
  VersionColumn,
} from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Category } from '../../category/entities/category.entity';

export enum ProductStatus {
  ON_SALE = 'ON_SALE',
  SOLD_OUT = 'SOLD_OUT',
  HIDDEN = 'HIDDEN',
}

@Entity('products')
export class Product extends BaseEntity {
  @Column({ type: 'varchar', length: 200 })
  name: string;

  @Column({ type: 'text' })
  description: string;

  @Column({ type: 'int' })
  price: number;

  @Column({ name: 'discount_price', type: 'int', nullable: true })
  discountPrice: number | null;

  @Column({ type: 'int', default: 0 })
  stock: number;

  @Index('idx_products_status')
  @Column({ type: 'enum', enum: ProductStatus, default: ProductStatus.ON_SALE })
  status: ProductStatus;

  @Index('idx_products_category')
  @Column({ name: 'category_id', type: 'int' })
  categoryId: number;

  @Column({ name: 'thumbnail_url', type: 'varchar', length: 500, nullable: true })
  thumbnailUrl: string | null;

  @Index('idx_products_lowest_price')
  @Column({ name: 'lowest_price', type: 'int', nullable: true })
  lowestPrice: number | null;

  @Column({ name: 'seller_count', type: 'int', default: 0 })
  sellerCount: number;

  @Index('idx_products_view_count')
  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @Column({ name: 'review_count', type: 'int', default: 0 })
  reviewCount: number;

  @Column({ name: 'average_rating', type: 'decimal', precision: 2, scale: 1, default: 0.0 })
  averageRating: number;

  @Column({ name: 'sales_count', type: 'int', default: 0 })
  salesCount: number;

  @Index('idx_products_popularity')
  @Column({ name: 'popularity_score', type: 'decimal', precision: 10, scale: 2, default: 0 })
  popularityScore: number;

  @VersionColumn()
  version: number;

  @ManyToOne(() => Category, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'category_id' })
  category: Category;

  @OneToMany('ProductOption', 'product')
  options: any[];

  @OneToMany('ProductImage', 'product')
  images: any[];

  @OneToMany('ProductSpec', 'product')
  specs: any[];
}
