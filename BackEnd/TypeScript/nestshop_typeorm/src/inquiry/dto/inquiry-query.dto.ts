import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsOptional } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { InquiryStatus } from '../entities/inquiry.entity';

export class InquiryQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '문의 상태 필터', enum: InquiryStatus })
  @IsOptional()
  @IsEnum(InquiryStatus)
  status?: InquiryStatus;
}
