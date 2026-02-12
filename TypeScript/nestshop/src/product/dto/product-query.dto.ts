import { IsOptional, IsInt, IsString, IsEnum, Min, Max } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export enum ProductSort {
  NEWEST = 'newest',
  POPULARITY = 'popularity',
  PRICE_ASC = 'price_asc',
  PRICE_DESC = 'price_desc',
  RATING_DESC = 'rating_desc',
  RATING_ASC = 'rating_asc',
}

export class ProductQueryDto {
  @ApiPropertyOptional({ default: 1 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page: number = 1;

  @ApiPropertyOptional({ default: 20, maximum: 100 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(100)
  limit: number = 20;

  @ApiPropertyOptional({ description: '카테고리 ID' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  categoryId?: number;

  @ApiPropertyOptional({ description: '검색어' })
  @IsOptional()
  @IsString()
  search?: string;

  @ApiPropertyOptional({ description: '최소 가격' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  minPrice?: number;

  @ApiPropertyOptional({ description: '최대 가격' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  maxPrice?: number;

  @ApiPropertyOptional({ description: '정렬', enum: ProductSort, default: ProductSort.NEWEST })
  @IsOptional()
  @IsEnum(ProductSort)
  sort?: ProductSort;

  @ApiPropertyOptional({ description: '스펙 필터 (JSON)' })
  @IsOptional()
  @IsString()
  specs?: string;

  get skip(): number {
    return (this.page - 1) * this.limit;
  }
}
