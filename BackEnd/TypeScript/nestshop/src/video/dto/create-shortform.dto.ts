import { Transform, Type } from 'class-transformer';
import { IsArray, IsInt, IsOptional, IsString, Length, Max, Min } from 'class-validator';

export class CreateShortformDto {
  @IsString()
  @Length(1, 120)
  title: string;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(60)
  durationSec?: number;

  @IsOptional()
  @Transform(({ value }) => {
    if (Array.isArray(value)) return value;
    if (typeof value === 'string') {
      try {
        const parsed = JSON.parse(value);
        if (Array.isArray(parsed)) return parsed;
      } catch {
        return value.split(',').map((item) => item.trim()).filter(Boolean);
      }
    }
    return [];
  })
  @IsArray()
  @Type(() => Number)
  @IsInt({ each: true })
  @Min(1, { each: true })
  productIds?: number[];
}
