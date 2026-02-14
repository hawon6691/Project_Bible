import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { ChatRoom } from './chat-room.entity';

@Entity('chat_room_members')
export class ChatRoomMember extends BaseEntity {
  @Index('idx_chat_room_members_room_id')
  @Column({ name: 'room_id', type: 'int' })
  roomId: number;

  @Index('idx_chat_room_members_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ name: 'joined_at', type: 'timestamp' })
  joinedAt: Date;

  @ManyToOne(() => ChatRoom, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'room_id' })
  room: ChatRoom;
}
