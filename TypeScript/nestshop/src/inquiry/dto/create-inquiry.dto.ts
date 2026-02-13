import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional, IsString, Length } from 'class-validator';

export class CreateInquiryDto {
  @ApiProperty({ description: '문의 제목', minLength: 1, maxLength: 120 })
  @IsString()
  @Length(1, 120)
  title: string;

  @ApiProperty({ description: '문의 내용', minLength: 1, maxLength: 3000 })
  @IsString()
  @Length(1, 3000)
  content: string;

  @ApiPropertyOptional({ description: '비밀글 여부', default: false })
  @IsOptional()
  @IsBoolean()
  isSecret?: boolean;
}
