import {
  IsString,
  IsInt,
  IsOptional,
  IsEnum,
  IsArray,
  IsBoolean,
  MaxLength,
  Min,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { SpecInputType, SpecDataType } from '../entities/spec-definition.entity';

export class CreateSpecDefinitionDto {
  @ApiProperty({ description: '카테고리 ID', example: 1 })
  @IsInt()
  categoryId: number;

  @ApiProperty({ description: '스펙명', example: 'CPU' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ description: '입력 타입', enum: SpecInputType })
  @IsEnum(SpecInputType)
  type: SpecInputType;

  @ApiPropertyOptional({ description: 'SELECT 타입 선택지', example: ['i5', 'i7', 'i9'] })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  options?: string[];

  @ApiPropertyOptional({ description: '단위', example: 'GB' })
  @IsOptional()
  @IsString()
  @MaxLength(20)
  unit?: string;

  @ApiPropertyOptional({ description: '스펙 그룹명', example: '성능' })
  @IsOptional()
  @IsString()
  @MaxLength(50)
  groupName?: string;

  @ApiPropertyOptional({ description: '부모 스펙 정의 ID (상속 구조)' })
  @IsOptional()
  @IsInt()
  @Min(1)
  parentDefinitionId?: number;

  @ApiPropertyOptional({ description: '수치 비교 시 높을수록 좋은지 여부', default: true })
  @IsOptional()
  @IsBoolean()
  higherIsBetter?: boolean;

  @ApiPropertyOptional({ description: '비교 대상 여부', default: true })
  @IsOptional()
  @IsBoolean()
  isComparable?: boolean;

  @ApiPropertyOptional({ description: '데이터 타입', enum: SpecDataType })
  @IsOptional()
  @IsEnum(SpecDataType)
  dataType?: SpecDataType;

  @ApiPropertyOptional({ description: '정렬 순서', default: 0 })
  @IsOptional()
  @IsInt()
  @Min(0)
  sortOrder?: number;
}
