import {
  IsString,
  IsOptional,
  IsEnum,
  IsArray,
  IsBoolean,
  IsInt,
  MaxLength,
  Min,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { SpecInputType, SpecDataType } from '../entities/spec-definition.entity';

export class UpdateSpecDefinitionDto {
  @ApiPropertyOptional({ description: '스펙명' })
  @IsOptional()
  @IsString()
  @MaxLength(50)
  name?: string;

  @ApiPropertyOptional({ description: '입력 타입', enum: SpecInputType })
  @IsOptional()
  @IsEnum(SpecInputType)
  type?: SpecInputType;

  @ApiPropertyOptional({ description: 'SELECT 타입 선택지' })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  options?: string[];

  @ApiPropertyOptional({ description: '단위' })
  @IsOptional()
  @IsString()
  @MaxLength(20)
  unit?: string;

  @ApiPropertyOptional({ description: '비교 대상 여부' })
  @IsOptional()
  @IsBoolean()
  isComparable?: boolean;

  @ApiPropertyOptional({ description: '데이터 타입', enum: SpecDataType })
  @IsOptional()
  @IsEnum(SpecDataType)
  dataType?: SpecDataType;

  @ApiPropertyOptional({ description: '정렬 순서' })
  @IsOptional()
  @IsInt()
  @Min(0)
  sortOrder?: number;
}
