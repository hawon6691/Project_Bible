import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsOptional, Max, Min } from 'class-validator';

export class PricePredictionQueryDto {
  @ApiPropertyOptional({ description: '예측할 미래 일수', default: 7, minimum: 1, maximum: 30 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(30)
  horizonDays: number = 7;

  @ApiPropertyOptional({ description: '분석에 사용할 과거 일수', default: 30, minimum: 7, maximum: 180 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(7)
  @Max(180)
  lookbackDays: number = 30;
}
