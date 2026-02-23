import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsOptional } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';

export class FailedJobsQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '최근 실패 Job 우선 정렬 여부', default: true })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  newestFirst?: boolean = true;
}
