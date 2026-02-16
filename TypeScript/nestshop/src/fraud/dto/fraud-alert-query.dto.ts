import { Type } from 'class-transformer';
import { IsEnum, IsOptional } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { FraudFlagStatus } from '../entities/fraud-flag.entity';

export class FraudAlertQueryDto extends PaginationRequestDto {
  @IsOptional()
  @IsEnum(FraudFlagStatus)
  status?: FraudFlagStatus;
}
