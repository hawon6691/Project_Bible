import {
  Entity,
  Column,
  ManyToOne,
  JoinColumn,
  Index,
  Unique,
} from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';
import { Product } from '../../product/entities/product.entity';
import { Order } from '../../order/entities/order.entity';

@Entity('reviews')
@Unique('uq_reviews_user_order_product', ['userId', 'orderId', 'productId'])
export class Review extends BaseEntity {
  @Index('idx_reviews_user')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_reviews_product')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @Column({ name: 'order_id', type: 'int' })
  orderId: number;

  @Column({ type: 'int' })
  rating: number;

  @Column({ type: 'text' })
  content: string;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Product, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'product_id' })
  product: Product;

  @ManyToOne(() => Order, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'order_id' })
  order: Order;
}
