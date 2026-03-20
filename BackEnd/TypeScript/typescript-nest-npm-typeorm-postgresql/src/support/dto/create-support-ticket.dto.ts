import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, Length } from 'class-validator';

export class CreateSupportTicketDto {
  @ApiProperty({ description: '문의 카테고리', example: '결제/환불' })
  @IsString()
  @Length(1, 50)
  category: string;

  @ApiProperty({ description: '문의 제목', minLength: 1, maxLength: 120 })
  @IsString()
  @Length(1, 120)
  title: string;

  @ApiProperty({ description: '문의 내용', minLength: 1, maxLength: 3000 })
  @IsString()
  @Length(1, 3000)
  content: string;

  @ApiPropertyOptional({ description: '첨부 파일 URL' })
  @IsOptional()
  @IsString()
  @Length(1, 500)
  attachmentUrl?: string;
}
