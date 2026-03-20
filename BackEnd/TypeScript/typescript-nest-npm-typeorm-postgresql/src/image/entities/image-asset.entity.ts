import { Column, Entity, Index, OneToMany } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { ImageVariant } from './image-variant.entity';

export enum ImageCategory {
  PRODUCT = 'product',
  COMMUNITY = 'community',
  SUPPORT = 'support',
  SELLER = 'seller',
}

export enum ImageProcessingStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

@Entity('image_assets')
export class ImageAsset extends BaseEntity {
  @Column({ name: 'uploaded_by_user_id', type: 'int', nullable: true })
  uploadedByUserId: number | null;

  @Column({ name: 'original_filename', type: 'varchar', length: 255 })
  originalFilename: string;

  @Index('idx_image_assets_stored_filename', { unique: true })
  @Column({ name: 'stored_filename', type: 'varchar', length: 255 })
  storedFilename: string;

  @Column({ name: 'original_url', type: 'varchar', length: 500 })
  originalUrl: string;

  @Column({ name: 'mime_type', type: 'varchar', length: 100 })
  mimeType: string;

  @Column({ type: 'int' })
  size: number;

  @Index('idx_image_assets_category')
  @Column({ type: 'enum', enum: ImageCategory })
  category: ImageCategory;

  @Index('idx_image_assets_processing_status')
  @Column({ name: 'processing_status', type: 'enum', enum: ImageProcessingStatus, default: ImageProcessingStatus.PENDING })
  processingStatus: ImageProcessingStatus;

  @OneToMany(() => ImageVariant, (variant) => variant.imageAsset)
  variants: ImageVariant[];
}
