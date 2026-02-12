import { IsArray, IsInt, ArrayMinSize, ArrayMaxSize, IsOptional, IsObject } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CompareSpecsDto {
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', type: [Number] })
  @IsArray()
  @IsInt({ each: true })
  @ArrayMinSize(2)
  @ArrayMaxSize(4)
  productIds: number[];
}

export class ScoredCompareDto {
  @ApiProperty({ description: '비교할 상품 ID 목록 (2~4개)', type: [Number] })
  @IsArray()
  @IsInt({ each: true })
  @ArrayMinSize(2)
  @ArrayMaxSize(4)
  productIds: number[];

  @ApiPropertyOptional({ description: '가중치 (합계 100)', example: { CPU: 30, RAM: 25, GPU: 25, "무게": 20 } })
  @IsOptional()
  @IsObject()
  weights?: Record<string, number>;
}

export class SetSpecScoresDto {
  @ApiProperty({ description: '스펙 점수 매핑', type: 'array', items: { type: 'object' } })
  @IsArray()
  scores: { value: string; score: number }[];
}
