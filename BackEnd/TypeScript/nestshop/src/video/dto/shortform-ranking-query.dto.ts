import { Type } from 'class-transformer';
import { IsEnum, IsOptional } from 'class-validator';

export enum ShortformRankingPeriod {
  DAY = 'day',
  WEEK = 'week',
  MONTH = 'month',
}

export class ShortformRankingQueryDto {
  @IsOptional()
  @IsEnum(ShortformRankingPeriod)
  period?: ShortformRankingPeriod = ShortformRankingPeriod.DAY;

  @IsOptional()
  @Type(() => Number)
  limit?: number = 20;
}
