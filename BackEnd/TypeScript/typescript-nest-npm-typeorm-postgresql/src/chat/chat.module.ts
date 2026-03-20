import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ChatController } from './chat.controller';
import { ChatGateway } from './chat.gateway';
import { ChatService } from './chat.service';
import { ChatMessage } from './entities/chat-message.entity';
import { ChatRoomMember } from './entities/chat-room-member.entity';
import { ChatRoom } from './entities/chat-room.entity';

@Module({
  imports: [TypeOrmModule.forFeature([ChatRoom, ChatRoomMember, ChatMessage])],
  controllers: [ChatController],
  providers: [ChatService, ChatGateway],
  exports: [ChatService],
})
export class ChatModule {}
