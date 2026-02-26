import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsOptional, Max, Min } from 'class-validator';

export class FraudScanQueryDto {
  @ApiPropertyOptional({ description: '탐지 민감도(기본 50% 이하를 이상값)', default: 0.5 })
  @IsOptional()
  lowerBoundRatio?: number;

  @ApiPropertyOptional({ description: '상단 임계치(기본 180% 이상을 이상값)', default: 1.8 })
  @IsOptional()
  upperBoundRatio?: number;

  @ApiPropertyOptional({ description: '조회 개수 제한', default: 100, minimum: 1, maximum: 500 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(500)
  limit: number = 100;
}
