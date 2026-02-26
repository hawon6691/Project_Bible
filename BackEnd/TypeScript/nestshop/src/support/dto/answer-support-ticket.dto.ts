import { ApiProperty } from '@nestjs/swagger';
import { IsString, Length } from 'class-validator';

export class AnswerSupportTicketDto {
  @ApiProperty({ description: '답변 내용', minLength: 1, maxLength: 3000 })
  @IsString()
  @Length(1, 3000)
  content: string;
}
