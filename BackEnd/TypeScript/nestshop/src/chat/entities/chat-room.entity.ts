import { Column, Entity, Index } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';

@Entity('chat_rooms')
export class ChatRoom extends BaseEntity {
  @Index('idx_chat_rooms_name')
  @Column({ type: 'varchar', length: 100 })
  name: string;

  @Index('idx_chat_rooms_created_by')
  @Column({ name: 'created_by', type: 'int' })
  createdBy: number;

  @Column({ name: 'is_private', type: 'boolean', default: true })
  isPrivate: boolean;
}
