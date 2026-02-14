import { Column, Entity, Index, JoinColumn, ManyToOne, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Product } from '../../product/entities/product.entity';
import { User } from '../../user/entities/user.entity';

@Entity('recent_product_views')
@Unique('uq_recent_product_view_user_product', ['userId', 'productId'])
export class RecentProductView extends BaseEntity {
  @Index('idx_recent_product_views_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_recent_product_views_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'viewed_at', type: 'timestamp' })
  viewedAt: Date;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;
}
