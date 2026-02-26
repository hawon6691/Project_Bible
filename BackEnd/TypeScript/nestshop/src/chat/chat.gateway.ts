import {
  ConnectedSocket,
  MessageBody,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from '@nestjs/websockets';
import { UseGuards } from '@nestjs/common';
import { Server, Socket } from 'socket.io';
import { WsAuthGuard } from '../common/guards/ws-auth.guard';
import { ChatService } from './chat.service';
import { JoinChatRoomDto } from './dto/join-chat-room.dto';
import { WsSendMessageDto } from './dto/ws-send-message.dto';

interface ChatSocket extends Socket {
  user?: {
    sub: number;
    email: string;
    role: string;
  };
}

@WebSocketGateway({
  namespace: '/chat',
  cors: {
    origin: '*',
    credentials: true,
  },
})
@UseGuards(WsAuthGuard)
export class ChatGateway {
  @WebSocketServer()
  server: Server;

  constructor(private readonly chatService: ChatService) {}

  // 클라이언트가 채팅방에 조인하면 소켓 room을 연결한다.
  @SubscribeMessage('joinRoom')
  async handleJoinRoom(@ConnectedSocket() client: ChatSocket, @MessageBody() payload: JoinChatRoomDto) {
    const userId = client.user?.sub;
    if (!userId) {
      return { ok: false, message: '인증 정보가 없습니다.' };
    }

    await this.chatService.joinRoom(userId, payload.roomId);
    await client.join(`room:${payload.roomId}`);

    return { ok: true, roomId: payload.roomId };
  }

  // 웹소켓 메시지 전송 시 DB 저장 후 동일 room에 브로드캐스트한다.
  @SubscribeMessage('sendMessage')
  async handleSendMessage(@ConnectedSocket() client: ChatSocket, @MessageBody() payload: WsSendMessageDto) {
    const userId = client.user?.sub;
    if (!userId) {
      return { ok: false, message: '인증 정보가 없습니다.' };
    }

    const message = await this.chatService.sendMessage(userId, payload.roomId, {
      message: payload.message,
    });

    this.server.to(`room:${payload.roomId}`).emit('newMessage', message);
    return { ok: true, message };
  }
}
