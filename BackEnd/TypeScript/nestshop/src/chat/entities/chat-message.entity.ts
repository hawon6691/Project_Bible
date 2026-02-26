import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { ChatRoom } from './chat-room.entity';

@Entity('chat_messages')
export class ChatMessage extends BaseEntity {
  @Index('idx_chat_messages_room_id')
  @Column({ name: 'room_id', type: 'int' })
  roomId: number;

  @Index('idx_chat_messages_sender_id')
  @Column({ name: 'sender_id', type: 'int' })
  senderId: number;

  @Column({ type: 'text' })
  message: string;

  @ManyToOne(() => ChatRoom, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'room_id' })
  room: ChatRoom;
}
