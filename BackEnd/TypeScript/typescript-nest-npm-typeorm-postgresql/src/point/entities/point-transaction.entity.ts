import {
  Entity,
  Column,
  ManyToOne,
  JoinColumn,
  Index,
} from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';
import { PointType } from '../../common/constants/point-type.enum';

// 포인트 적립/사용/환원 이력을 저장하는 트랜잭션 엔티티
@Entity('point_transactions')
export class PointTransaction extends BaseEntity {
  @Index('idx_point_transactions_user')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ type: 'enum', enum: PointType })
  type: PointType;

  @Column({ type: 'int' })
  amount: number;

  // 거래 후 사용자 보유 포인트 스냅샷
  @Column({ name: 'balance_after', type: 'int' })
  balanceAfter: number;

  @Column({ type: 'varchar', length: 200, nullable: true })
  description: string | null;

  // 주문 연관 포인트 거래일 경우 주문 ID 저장
  @Column({ name: 'order_id', type: 'int', nullable: true })
  orderId: number | null;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;
}
