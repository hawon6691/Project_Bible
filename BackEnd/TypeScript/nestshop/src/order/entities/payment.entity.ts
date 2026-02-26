import {
  Entity, Column, ManyToOne, JoinColumn, PrimaryGeneratedColumn,
  CreateDateColumn, UpdateDateColumn, Index,
} from 'typeorm';
import { Order } from './order.entity';

// 결제 수단 확장 대비를 위해 enum으로 관리한다.
export enum PaymentMethod {
  CARD = 'CARD',
  BANK_TRANSFER = 'BANK_TRANSFER',
  VIRTUAL_ACCOUNT = 'VIRTUAL_ACCOUNT',
}

// 결제 상태는 결제 요청 -> 완료/실패 -> 환불 순으로 전이된다.
export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
}

@Entity('payments')
export class Payment {
  @PrimaryGeneratedColumn()
  id: number;

  // 주문 단위로 결제 이력을 조회하기 위해 인덱스를 둔다.
  @Index('idx_payments_order')
  @Column({ name: 'order_id', type: 'int' })
  orderId: number;

  @Column({ type: 'enum', enum: PaymentMethod })
  method: PaymentMethod;

  @Column({ type: 'int' })
  amount: number;

  @Column({ type: 'enum', enum: PaymentStatus, default: PaymentStatus.PENDING })
  status: PaymentStatus;

  @Column({ name: 'paid_at', type: 'timestamp', nullable: true })
  paidAt: Date | null;

  @Column({ name: 'refunded_at', type: 'timestamp', nullable: true })
  refundedAt: Date | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ManyToOne(() => Order, (order) => order.payments, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'order_id' })
  order: Order;
}
