import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { PcPartType } from './pc-build-part.entity';

export enum CompatibilitySeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

@Entity('pc_compatibility_rules')
export class PcCompatibilityRule extends BaseEntity {
  @Index('idx_pc_rules_part_type')
  @Column({ name: 'part_type', type: 'enum', enum: PcPartType })
  partType: PcPartType;

  @Column({ name: 'target_part_type', type: 'enum', enum: PcPartType, nullable: true })
  targetPartType: PcPartType | null;

  @Column({ type: 'varchar', length: 100 })
  title: string;

  @Column({ type: 'varchar', length: 500 })
  description: string;

  @Column({ type: 'enum', enum: CompatibilitySeverity, default: CompatibilitySeverity.MEDIUM })
  severity: CompatibilitySeverity;

  @Column({ type: 'boolean', default: true })
  enabled: boolean;

  @Column({ type: 'jsonb', nullable: true })
  metadata: Record<string, unknown> | null;
}
