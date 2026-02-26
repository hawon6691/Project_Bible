import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsInt, IsOptional, Min } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';

export class CrawlerJobQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '판매처 ID 필터' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  sellerId?: number;

  @ApiPropertyOptional({ description: '활성화 여부 필터' })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isActive?: boolean;
}
