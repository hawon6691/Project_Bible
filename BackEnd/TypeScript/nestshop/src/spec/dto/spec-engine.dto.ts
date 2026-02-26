import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { ArrayMaxSize, ArrayMinSize, IsArray, IsInt, IsOptional, Min } from 'class-validator';

export class NumericCompareDto {
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', example: [1, 2, 3] })
  @IsArray()
  @IsInt({ each: true })
  @ArrayMinSize(2)
  @ArrayMaxSize(4)
  productIds: number[];
}

export class SimilarProductsQueryDto {
  @ApiPropertyOptional({ description: '추천 개수', default: 5, minimum: 1 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  limit: number = 5;
}

export class ScoreByCategoryDto {
  @ApiProperty({ description: '카테고리 ID' })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  categoryId: number;

  @ApiProperty({ description: '상품 ID 목록', example: [1, 2, 3] })
  @IsArray()
  @IsInt({ each: true })
  @ArrayMinSize(2)
  @ArrayMaxSize(4)
  productIds: number[];
}
