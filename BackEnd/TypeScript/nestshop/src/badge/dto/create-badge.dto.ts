import { Type } from 'class-transformer';
import { IsEnum, IsInt, IsObject, IsOptional, IsString, IsUrl, Length, Min, ValidateNested } from 'class-validator';
import { BadgeRarity, BadgeType } from '../entities/badge.entity';

export enum BadgeConditionMetric {
  REVIEW_COUNT = 'review_count',
  POST_COUNT = 'post_count',
  ORDER_COUNT = 'order_count',
  POINT_TOTAL = 'point_total',
  LOGIN_STREAK = 'login_streak',
}

export class BadgeConditionDto {
  @IsEnum(BadgeConditionMetric)
  metric: BadgeConditionMetric;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  threshold: number;
}

export class CreateBadgeDto {
  @IsString()
  @Length(1, 100)
  name: string;

  @IsString()
  @Length(1, 255)
  description: string;

  @IsUrl()
  @Length(1, 500)
  iconUrl: string;

  @IsEnum(BadgeType)
  type: BadgeType;

  @IsOptional()
  @IsObject()
  @ValidateNested()
  @Type(() => BadgeConditionDto)
  condition?: BadgeConditionDto;

  @IsEnum(BadgeRarity)
  rarity: BadgeRarity;
}
