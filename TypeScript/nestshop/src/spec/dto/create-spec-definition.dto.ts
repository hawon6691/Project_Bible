import {
  IsString, IsInt, IsOptional, IsArray, IsEnum, MaxLength, IsBoolean,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { SpecInputType, SpecDataType } from '../entities/spec-definition.entity';

export class CreateSpecDefinitionDto {
  @ApiProperty({ description: '카테고리 ID' })
  @IsInt()
  categoryId: number;

  @ApiProperty({ description: '스펙 항목명' })
  @IsString()
  @MaxLength(100)
  name: string;

  @ApiPropertyOptional({ description: '입력 유형', enum: SpecInputType })
  @IsOptional()
  @IsEnum(SpecInputType)
  inputType?: SpecInputType;

  @ApiPropertyOptional({ description: '데이터 유형', enum: SpecDataType })
  @IsOptional()
  @IsEnum(SpecDataType)
  dataType?: SpecDataType;

  @ApiPropertyOptional({ description: '선택 옵션 목록', type: [String] })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  options?: string[];

  @ApiPropertyOptional({ description: '단위' })
  @IsOptional()
  @IsString()
  @MaxLength(20)
  unit?: string;
}
