import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

export enum AuctionStatus {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  CANCELLED = 'CANCELLED',
}

@Entity('auctions')
export class Auction extends BaseEntity {
  @Index('idx_auctions_owner_id')
  @Column({ name: 'owner_id', type: 'int' })
  ownerId: number;

  @Column({ type: 'varchar', length: 200 })
  title: string;

  @Column({ type: 'text' })
  description: string;

  @Index('idx_auctions_category_id')
  @Column({ name: 'category_id', type: 'int' })
  categoryId: number;

  @Column({ type: 'jsonb', nullable: true })
  specs: Record<string, unknown> | null;

  @Column({ type: 'int' })
  budget: number;

  @Index('idx_auctions_status')
  @Column({ type: 'enum', enum: AuctionStatus, default: AuctionStatus.OPEN })
  status: AuctionStatus;

  @Column({ name: 'selected_bid_id', type: 'int', nullable: true })
  selectedBidId: number | null;
}
