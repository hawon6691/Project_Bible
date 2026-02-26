import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('auction_bids')
export class AuctionBid extends BaseEntity {
  @Index('idx_auction_bids_auction_id')
  @Column({ name: 'auction_id', type: 'int' })
  auctionId: number;

  @Index('idx_auction_bids_seller_id')
  @Column({ name: 'seller_id', type: 'int' })
  sellerId: number;

  @Column({ type: 'int' })
  price: number;

  @Column({ type: 'varchar', length: 500, nullable: true })
  description: string | null;

  @Column({ name: 'delivery_days', type: 'int' })
  deliveryDays: number;
}
