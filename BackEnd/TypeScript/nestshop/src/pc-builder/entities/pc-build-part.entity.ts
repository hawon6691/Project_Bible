import { Column, Entity, Index, JoinColumn, ManyToOne, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Product } from '../../product/entities/product.entity';
import { Seller } from '../../seller/entities/seller.entity';
import { PcBuild } from './pc-build.entity';

export enum PcPartType {
  CPU = 'CPU',
  MOTHERBOARD = 'MOTHERBOARD',
  RAM = 'RAM',
  GPU = 'GPU',
  SSD = 'SSD',
  HDD = 'HDD',
  PSU = 'PSU',
  CASE = 'CASE',
  COOLER = 'COOLER',
  MONITOR = 'MONITOR',
}

@Entity('pc_build_parts')
@Unique('uq_pc_build_parts_build_part_type', ['buildId', 'partType'])
export class PcBuildPart extends BaseEntity {
  @Index('idx_pc_build_parts_build_id')
  @Column({ name: 'build_id', type: 'int' })
  buildId: number;

  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ name: 'part_type', type: 'enum', enum: PcPartType })
  partType: PcPartType;

  @Column({ type: 'int', default: 1 })
  quantity: number;

  @Column({ name: 'unit_price', type: 'int' })
  unitPrice: number;

  @Column({ name: 'total_price', type: 'int' })
  totalPrice: number;

  @ManyToOne(() => PcBuild, (build) => build.parts, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'build_id' })
  build: PcBuild;

  @ManyToOne(() => Product, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'product_id' })
  product: Product;

  @ManyToOne(() => Seller, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'seller_id' })
  seller: Seller;
}
