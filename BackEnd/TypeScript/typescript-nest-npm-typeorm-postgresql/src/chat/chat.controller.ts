import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { ChatService } from './chat.service';
import { CreateChatRoomDto } from './dto/create-chat-room.dto';
import { SendChatMessageDto } from './dto/send-chat-message.dto';

@ApiTags('Chat')
@Controller('chat')
@ApiBearerAuth()
export class ChatController {
  constructor(private readonly chatService: ChatService) {}

  // CHAT-01: 채팅방 목록
  @Get('rooms')
  @ApiOperation({ summary: '내 채팅방 목록 조회' })
  findMyRooms(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.chatService.findMyRooms(user.sub, query);
  }

  // CHAT-02: 채팅방 생성
  @Post('rooms')
  @ApiOperation({ summary: '채팅방 생성' })
  createRoom(@CurrentUser() user: JwtPayload, @Body() dto: CreateChatRoomDto) {
    return this.chatService.createRoom(user.sub, dto);
  }

  // 채팅방 입장
  @Post('rooms/:id/join')
  @ApiOperation({ summary: '채팅방 입장' })
  joinRoom(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.chatService.joinRoom(user.sub, id);
  }

  // CHAT-03: 메시지 목록 조회
  @Get('rooms/:id/messages')
  @ApiOperation({ summary: '채팅 메시지 목록 조회' })
  findMessages(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Query() query: PaginationRequestDto,
  ) {
    return this.chatService.findMessages(user.sub, id, query);
  }

  // CHAT-04: 메시지 전송
  @Post('rooms/:id/messages')
  @ApiOperation({ summary: '채팅 메시지 전송' })
  sendMessage(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: SendChatMessageDto,
  ) {
    return this.chatService.sendMessage(user.sub, id, dto);
  }
}
