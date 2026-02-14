import { Column, Entity, Index, JoinColumn, ManyToOne, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Shortform } from './shortform.entity';

@Entity('shortform_products')
@Unique('uq_shortform_products_shortform_product', ['shortformId', 'productId'])
export class ShortformProduct extends BaseEntity {
  @Index('idx_shortform_products_shortform_id')
  @Column({ name: 'shortform_id', type: 'int' })
  shortformId: number;

  @Index('idx_shortform_products_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @ManyToOne(() => Shortform, (shortform) => shortform.products, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'shortform_id' })
  shortform: Shortform;
}
