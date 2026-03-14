import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsOptional } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { SupportTicketStatus } from '../entities/support-ticket.entity';

export class SupportTicketQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '문의 상태 필터', enum: SupportTicketStatus })
  @IsOptional()
  @IsEnum(SupportTicketStatus)
  status?: SupportTicketStatus;
}
