import { ApiProperty } from '@nestjs/swagger';
import { IsString, Length } from 'class-validator';

export class SendChatMessageDto {
  @ApiProperty({ description: '메시지 본문', minLength: 1, maxLength: 1000 })
  @IsString()
  @Length(1, 1000)
  message: string;
}
