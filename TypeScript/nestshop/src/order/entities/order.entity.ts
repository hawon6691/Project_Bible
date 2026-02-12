import {
  Entity, Column, ManyToOne, OneToMany, JoinColumn, Index, VersionColumn,
} from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';

export enum OrderStatus {
  ORDER_PLACED = 'ORDER_PLACED',
  PAYMENT_PENDING = 'PAYMENT_PENDING',
  PAYMENT_CONFIRMED = 'PAYMENT_CONFIRMED',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  RETURN_REQUESTED = 'RETURN_REQUESTED',
  RETURNED = 'RETURNED',
}

@Entity('orders')
export class Order extends BaseEntity {
  @Column({ name: 'order_number', type: 'varchar', length: 30, unique: true })
  orderNumber: string;

  @Index('idx_orders_user')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_orders_status')
  @Column({ type: 'enum', enum: OrderStatus, default: OrderStatus.ORDER_PLACED })
  status: OrderStatus;

  @Column({ name: 'total_amount', type: 'int' })
  totalAmount: number;

  @Column({ name: 'point_used', type: 'int', default: 0 })
  pointUsed: number;

  @Column({ name: 'final_amount', type: 'int' })
  finalAmount: number;

  @Column({ name: 'recipient_name', type: 'varchar', length: 50 })
  recipientName: string;

  @Column({ name: 'recipient_phone', type: 'varchar', length: 20 })
  recipientPhone: string;

  @Column({ name: 'zip_code', type: 'varchar', length: 10 })
  zipCode: string;

  @Column({ type: 'varchar', length: 200 })
  address: string;

  @Column({ name: 'address_detail', type: 'varchar', length: 100, nullable: true })
  addressDetail: string | null;

  @Column({ type: 'varchar', length: 200, nullable: true })
  memo: string | null;

  @VersionColumn()
  version: number;

  @ManyToOne(() => User, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @OneToMany('OrderItem', 'order')
  items: any[];
}
