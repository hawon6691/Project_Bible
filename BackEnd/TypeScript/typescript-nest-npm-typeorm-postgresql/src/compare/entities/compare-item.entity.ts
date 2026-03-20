import { Column, Entity, Index, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('compare_items')
@Unique('uq_compare_items_compare_key_product_id', ['compareKey', 'productId'])
export class CompareItem extends BaseEntity {
  @Index('idx_compare_items_compare_key')
  @Column({ name: 'compare_key', type: 'varchar', length: 100 })
  compareKey: string;

  @Index('idx_compare_items_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'sort_order', type: 'int', default: 0 })
  sortOrder: number;
}
