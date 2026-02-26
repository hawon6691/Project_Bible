import {
  Entity,
  Column,
  ManyToOne,
  JoinColumn,
  PrimaryGeneratedColumn,
  CreateDateColumn,
  Index,
} from 'typeorm';
import { Category } from '../../category/entities/category.entity';

export enum SpecInputType {
  TEXT = 'TEXT',
  NUMBER = 'NUMBER',
  SELECT = 'SELECT',
}

export enum SpecDataType {
  NUMBER = 'NUMBER',
  STRING = 'STRING',
  BOOLEAN = 'BOOLEAN',
}

@Entity('spec_definitions')
export class SpecDefinition {
  @PrimaryGeneratedColumn()
  id: number;

  @Index('idx_spec_definitions_category')
  @Column({ name: 'category_id', type: 'int' })
  categoryId: number;

  @Column({ type: 'varchar', length: 50 })
  name: string;

  @Column({ type: 'enum', enum: SpecInputType })
  type: SpecInputType;

  @Column({ type: 'json', nullable: true })
  options: string[] | null;

  @Column({ type: 'varchar', length: 20, nullable: true })
  unit: string | null;

  @Column({ name: 'group_name', type: 'varchar', length: 50, nullable: true })
  groupName: string | null;

  @Column({ name: 'parent_definition_id', type: 'int', nullable: true })
  parentDefinitionId: number | null;

  @Column({ name: 'higher_is_better', type: 'boolean', default: true })
  higherIsBetter: boolean;

  @Column({ name: 'is_comparable', type: 'boolean', default: true })
  isComparable: boolean;

  @Column({ name: 'data_type', type: 'enum', enum: SpecDataType, default: SpecDataType.STRING })
  dataType: SpecDataType;

  @Column({ name: 'sort_order', type: 'int', default: 0 })
  sortOrder: number;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ManyToOne(() => Category, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'category_id' })
  category: Category;
}
