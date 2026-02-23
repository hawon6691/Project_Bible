import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsIn, IsOptional } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';

export class FailedJobsQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '최근 실패 Job 우선 정렬 여부', default: true })
  @IsOptional()
  @IsIn(['true', 'false'])
  newestFirst?: 'true' | 'false';
}
