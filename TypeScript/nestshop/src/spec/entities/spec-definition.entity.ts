import {
  Entity, Column, PrimaryGeneratedColumn, CreateDateColumn,
  UpdateDateColumn, Index,
} from 'typeorm';
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
  STRING = 'STRING',
  NUMBER = 'NUMBER',
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

  @Column({ type: 'varchar', length: 100 })
  name: string;

  @Column({ name: 'input_type', type: 'enum', enum: SpecInputType, default: SpecInputType.TEXT })
  inputType: SpecInputType;

  @Column({ name: 'data_type', type: 'enum', enum: SpecDataType, default: SpecDataType.STRING })
  dataType: SpecDataType;
  @Column({ type: 'varchar', length: 50 })
  name: string;

  @Column({ type: 'enum', enum: SpecInputType })
  type: SpecInputType;

  @Column({ type: 'json', nullable: true })
  options: string[] | null;

  @Column({ type: 'varchar', length: 20, nullable: true })
  unit: string | null;

  @Column({ name: 'sort_order', type: 'int', default: 0 })
  sortOrder: number;

  @Column({ name: 'is_filterable', type: 'boolean', default: true })
  isFilterable: boolean;

  @Column({ name: 'is_comparable', type: 'boolean', default: true })
  isComparable: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;
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
