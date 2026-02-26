import {
  Entity,
  Column,
  ManyToOne,
  JoinColumn,
  PrimaryGeneratedColumn,
  CreateDateColumn,
  UpdateDateColumn,
  Unique,
  Index,
} from 'typeorm';
import { SpecDefinition } from './spec-definition.entity';

@Entity('spec_scores')
@Unique('uq_spec_scores', ['specDefinitionId', 'value'])
export class SpecScore {
  @PrimaryGeneratedColumn()
  id: number;

  @Index('idx_spec_scores_definition')
  @Column({ name: 'spec_definition_id', type: 'int' })
  specDefinitionId: number;

  @Column({ type: 'varchar', length: 200 })
  value: string;

  @Column({ type: 'int' })
  score: number;

  @Column({ name: 'benchmark_source', type: 'varchar', length: 100, nullable: true })
  benchmarkSource: string | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => SpecDefinition, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'spec_definition_id' })
  specDefinition: SpecDefinition;
}
