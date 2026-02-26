import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('product_query_views')
export class ProductQueryView extends BaseEntity {
  @Index('idx_product_query_views_product_id', { unique: true })
  @Column({ name: 'product_id', type: 'int', unique: true })
  productId: number;

  @Index('idx_product_query_views_category_id')
  @Column({ name: 'category_id', type: 'int' })
  categoryId: number;

  @Column({ type: 'varchar', length: 200 })
  name: string;

  @Column({ name: 'thumbnail_url', type: 'varchar', length: 500, nullable: true })
  thumbnailUrl: string | null;

  @Column({ name: 'status', type: 'varchar', length: 20 })
  status: string;

  @Column({ name: 'base_price', type: 'int' })
  basePrice: number;

  @Column({ name: 'lowest_price', type: 'int', nullable: true })
  lowestPrice: number | null;

  @Column({ name: 'seller_count', type: 'int', default: 0 })
  sellerCount: number;

  @Column({ name: 'average_rating', type: 'decimal', precision: 3, scale: 2, default: 0 })
  averageRating: number;

  @Column({ name: 'review_count', type: 'int', default: 0 })
  reviewCount: number;

  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @Column({ name: 'popularity_score', type: 'decimal', precision: 10, scale: 2, default: 0 })
  popularityScore: number;

  @Column({ name: 'synced_at', type: 'timestamp' })
  syncedAt: Date;
}
