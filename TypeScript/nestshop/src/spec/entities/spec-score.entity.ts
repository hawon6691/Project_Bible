import {
  Entity, Column, PrimaryGeneratedColumn, ManyToOne, JoinColumn, Unique,
} from 'typeorm';
import { SpecDefinition } from './spec-definition.entity';

@Entity('spec_scores')
@Unique('uq_spec_score', ['specDefinitionId', 'value'])
export class SpecScore {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'spec_definition_id', type: 'int' })
  specDefinitionId: number;

  @Column({ type: 'varchar', length: 500 })
  value: string;

  @Column({ type: 'int' })
  score: number;

  @ManyToOne(() => SpecDefinition, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'spec_definition_id' })
  specDefinition: SpecDefinition;
}
