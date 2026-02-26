import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('addresses')
export class Address extends BaseEntity {
  @Index('idx_addresses_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ name: 'recipient_name', type: 'varchar', length: 50 })
  recipientName: string;

  @Column({ type: 'varchar', length: 20 })
  phone: string;

  @Column({ name: 'zip_code', type: 'varchar', length: 10 })
  zipCode: string;

  @Column({ type: 'varchar', length: 255 })
  address: string;

  @Column({ name: 'address_detail', type: 'varchar', length: 255, nullable: true })
  addressDetail: string | null;

  @Column({ name: 'is_default', type: 'boolean', default: false })
  isDefault: boolean;
}
