import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { ImageAsset } from './image-asset.entity';

export enum ImageVariantType {
  THUMBNAIL = 'THUMBNAIL',
  MEDIUM = 'MEDIUM',
  LARGE = 'LARGE',
}

@Entity('image_variants')
export class ImageVariant extends BaseEntity {
  @Index('idx_image_variants_image_id')
  @Column({ name: 'image_id', type: 'int' })
  imageId: number;

  @Column({ type: 'enum', enum: ImageVariantType })
  type: ImageVariantType;

  @Column({ type: 'varchar', length: 500 })
  url: string;

  @Column({ type: 'int' })
  width: number;

  @Column({ type: 'int' })
  height: number;

  @Column({ type: 'varchar', length: 10 })
  format: string;

  @Column({ type: 'int' })
  size: number;

  @ManyToOne(() => ImageAsset, (imageAsset) => imageAsset.variants, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'image_id' })
  imageAsset: ImageAsset;
}
