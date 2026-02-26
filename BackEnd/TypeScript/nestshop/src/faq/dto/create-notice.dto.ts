import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional, IsString, Length } from 'class-validator';

export class CreateNoticeDto {
  @ApiProperty({ description: '공지 제목', minLength: 1, maxLength: 200 })
  @IsString()
  @Length(1, 200)
  title: string;

  @ApiProperty({ description: '공지 내용', minLength: 1, maxLength: 10000 })
  @IsString()
  @Length(1, 10000)
  content: string;

  @ApiPropertyOptional({ description: '게시 여부', default: true })
  @IsOptional()
  @IsBoolean()
  isPublished?: boolean;
}
