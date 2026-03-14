import { ApiProperty } from '@nestjs/swagger';
import { IsInt } from 'class-validator';

export class JoinChatRoomDto {
  @ApiProperty({ description: '채팅방 ID' })
  @IsInt()
  roomId: number;
}
