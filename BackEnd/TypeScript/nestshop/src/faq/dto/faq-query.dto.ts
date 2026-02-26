import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, MaxLength } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';

export class FaqQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '카테고리 필터' })
  @IsOptional()
  @IsString()
  @MaxLength(50)
  category?: string;

  @ApiPropertyOptional({ description: '검색어(질문/답변)', maxLength: 100 })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  keyword?: string;
}
