import { Column, Entity, Index, OneToMany } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { PcBuildPart } from './pc-build-part.entity';

export enum PcBuildPurpose {
  GAMING = 'GAMING',
  OFFICE = 'OFFICE',
  DESIGN = 'DESIGN',
  DEVELOPMENT = 'DEVELOPMENT',
  STREAMING = 'STREAMING',
}

@Entity('pc_builds')
export class PcBuild extends BaseEntity {
  @Index('idx_pc_builds_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ type: 'varchar', length: 120 })
  name: string;

  @Column({ type: 'varchar', length: 500, nullable: true })
  description: string | null;

  @Index('idx_pc_builds_purpose')
  @Column({ type: 'enum', enum: PcBuildPurpose })
  purpose: PcBuildPurpose;

  @Column({ type: 'int', nullable: true })
  budget: number | null;

  @Column({ name: 'total_price', type: 'int', default: 0 })
  totalPrice: number;

  @Index('idx_pc_builds_share_code', { unique: true })
  @Column({ name: 'share_code', type: 'varchar', length: 20, nullable: true, unique: true })
  shareCode: string | null;

  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @OneToMany(() => PcBuildPart, (part) => part.build)
  parts: PcBuildPart[];
}
