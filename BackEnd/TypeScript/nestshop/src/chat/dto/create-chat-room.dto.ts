import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional, IsString, Length } from 'class-validator';

export class CreateChatRoomDto {
  @ApiProperty({ description: '채팅방 이름', minLength: 1, maxLength: 100 })
  @IsString()
  @Length(1, 100)
  name: string;

  @ApiPropertyOptional({ description: '비공개 채팅방 여부', default: true })
  @IsOptional()
  @IsBoolean()
  isPrivate?: boolean;
}
