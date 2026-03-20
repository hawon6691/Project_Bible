import {
  Entity,
  Column,
  ManyToOne,
  JoinColumn,
  Unique,
  Index,
} from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';
import { Product } from '../../product/entities/product.entity';

@Entity('wishlists')
@Unique('uq_wishlist_user_product', ['userId', 'productId'])
export class Wishlist extends BaseEntity {
  @Index('idx_wishlist_user')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_wishlist_product')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;
}
