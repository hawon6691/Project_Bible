import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum MediaType {
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  DOCUMENT = 'DOCUMENT',
}

export enum MediaOwnerType {
  PRODUCT = 'PRODUCT',
  COMMUNITY = 'COMMUNITY',
  SUPPORT = 'SUPPORT',
  SELLER = 'SELLER',
  SHORTFORM = 'SHORTFORM',
  USER = 'USER',
}

@Entity('media_assets')
export class MediaAsset extends BaseEntity {
  @Index('idx_media_assets_uploader_id')
  @Column({ name: 'uploader_id', type: 'int' })
  uploaderId: number;

  @Column({ name: 'owner_type', type: 'enum', enum: MediaOwnerType })
  ownerType: MediaOwnerType;

  @Column({ name: 'owner_id', type: 'int' })
  ownerId: number;

  @Column({ name: 'original_name', type: 'varchar', length: 255 })
  originalName: string;

  @Column({ name: 'file_key', type: 'varchar', length: 500, unique: true })
  fileKey: string;

  @Column({ name: 'file_url', type: 'varchar', length: 500 })
  fileUrl: string;

  @Column({ type: 'enum', enum: MediaType })
  type: MediaType;

  @Column({ type: 'varchar', length: 120 })
  mime: string;

  @Column({ type: 'bigint' })
  size: string;

  @Column({ type: 'int', nullable: true })
  duration: number | null;

  @Column({ type: 'int', nullable: true })
  width: number | null;

  @Column({ type: 'int', nullable: true })
  height: number | null;
}
