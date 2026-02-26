import { ApiProperty } from '@nestjs/swagger';
import { IsInt, IsString, Length } from 'class-validator';

export class WsSendMessageDto {
  @ApiProperty({ description: '채팅방 ID' })
  @IsInt()
  roomId: number;

  @ApiProperty({ description: '메시지 내용', minLength: 1, maxLength: 1000 })
  @IsString()
  @Length(1, 1000)
  message: string;
}
