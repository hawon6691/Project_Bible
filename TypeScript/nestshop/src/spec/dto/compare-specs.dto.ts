import { IsArray, IsInt, ArrayMinSize, ArrayMaxSize, IsOptional, IsObject } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CompareSpecsDto {
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', type: [Number] })
import { IsArray, IsInt, IsOptional, ArrayMinSize, ArrayMaxSize } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CompareSpecsDto {
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', example: [1, 2, 3] })
  @IsArray()
  @IsInt({ each: true })
  @ArrayMinSize(2)
  @ArrayMaxSize(4)
  productIds: number[];
}

export class ScoredCompareDto {
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', type: [Number] })
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', example: [1, 2, 3] })
  @IsArray()
  @IsInt({ each: true })
  @ArrayMinSize(2)
  @ArrayMaxSize(4)
  productIds: number[];

  @ApiPropertyOptional({ description: '가중치 (합계 100)', example: { CPU: 30, RAM: 25, GPU: 25, "무게": 20 } })
  @IsOptional()
  @IsObject()
  @ApiPropertyOptional({
    description: '가중치 (합계 100)',
    example: { CPU: 30, RAM: 25, GPU: 25, '무게': 20 },
  })
  @IsOptional()
  weights?: Record<string, number>;
}

export class SetSpecScoresDto {
  @ApiProperty({ description: '스펙 점수 매핑', type: 'array', items: { type: 'object' } })
  @IsArray()
  scores: { value: string; score: number }[];
  @ApiProperty({
    description: '점수 매핑 목록',
    example: [{ value: 'i7-1360P', score: 78 }, { value: 'M3 Pro', score: 92 }],
  })
  @IsArray()
  scores: { value: string; score: number; benchmarkSource?: string }[];
}
