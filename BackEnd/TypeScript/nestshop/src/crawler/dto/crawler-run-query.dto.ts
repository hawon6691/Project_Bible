import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsEnum, IsInt, IsOptional, Min } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { CrawlerRunStatus } from '../entities/crawler-run.entity';

export class CrawlerRunQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '작업 ID 필터' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  jobId?: number;

  @ApiPropertyOptional({ description: '판매처 ID 필터' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  sellerId?: number;

  @ApiPropertyOptional({ description: '실행 상태 필터', enum: CrawlerRunStatus })
  @IsOptional()
  @IsEnum(CrawlerRunStatus)
  status?: CrawlerRunStatus;
}
