import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum ProductMappingStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

@Entity('product_mappings')
export class ProductMapping extends BaseEntity {
  @Column({ name: 'source_name', type: 'varchar', length: 200 })
  sourceName: string;

  @Column({ name: 'source_brand', type: 'varchar', length: 100, nullable: true })
  sourceBrand: string | null;

  @Column({ name: 'source_seller', type: 'varchar', length: 100, nullable: true })
  sourceSeller: string | null;

  @Column({ name: 'source_url', type: 'varchar', length: 500, nullable: true })
  sourceUrl: string | null;

  @Index('idx_product_mappings_status')
  @Column({ type: 'enum', enum: ProductMappingStatus, default: ProductMappingStatus.PENDING })
  status: ProductMappingStatus;

  @Index('idx_product_mappings_product_id')
  @Column({ name: 'product_id', type: 'int', nullable: true })
  productId: number | null;

  @Column({ type: 'decimal', precision: 5, scale: 2, default: 0 })
  confidence: string;

  @Column({ type: 'varchar', length: 255, nullable: true })
  reason: string | null;

  @Column({ name: 'reviewed_by', type: 'int', nullable: true })
  reviewedBy: number | null;

  @Column({ name: 'reviewed_at', type: 'timestamp', nullable: true })
  reviewedAt: Date | null;
}
