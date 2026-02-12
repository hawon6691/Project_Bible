import {
  Entity, Column, PrimaryGeneratedColumn, ManyToOne, JoinColumn,
  CreateDateColumn, Index, Unique,
} from 'typeorm';
import { Product } from '../../product/entities/product.entity';
import { SpecDefinition } from './spec-definition.entity';

@Entity('product_specs')
@Unique('uq_product_spec', ['productId', 'specDefinitionId'])
export class ProductSpec {
  @PrimaryGeneratedColumn()
  id: number;

  @Index('idx_product_specs_product')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'spec_definition_id', type: 'int' })
  specDefinitionId: number;

  @Column({ type: 'varchar', length: 500 })
  value: string;

  @Column({ name: 'numeric_value', type: 'decimal', precision: 15, scale: 4, nullable: true })
  numericValue: number | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ManyToOne(() => Product, (product) => product.specs, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;

  @ManyToOne(() => SpecDefinition, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'spec_definition_id' })
  specDefinition: SpecDefinition;
}
