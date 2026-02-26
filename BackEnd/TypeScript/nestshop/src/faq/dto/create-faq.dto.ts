import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional, IsString, Length } from 'class-validator';

export class CreateFaqDto {
  @ApiProperty({ description: 'FAQ 카테고리', example: '결제' })
  @IsString()
  @Length(1, 50)
  category: string;

  @ApiProperty({ description: '질문', minLength: 1, maxLength: 200 })
  @IsString()
  @Length(1, 200)
  question: string;

  @ApiProperty({ description: '답변', minLength: 1, maxLength: 5000 })
  @IsString()
  @Length(1, 5000)
  answer: string;

  @ApiPropertyOptional({ description: '활성 여부', default: true })
  @IsOptional()
  @IsBoolean()
  isActive?: boolean;
}
