import { IsArray, ValidateNested, IsInt, IsString, IsOptional, IsNumber } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export class ProductSpecItemDto {
  @ApiProperty({ description: '스펙 정의 ID' })
  @IsInt()
  specDefinitionId: number;

  @ApiProperty({ description: '스펙 값' })
  @IsString()
  value: string;

  @ApiPropertyOptional({ description: '숫자형 값' })
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class ProductSpecItemDto {
  @ApiProperty({ description: '스펙 정의 ID', example: 1 })
  @IsInt()
  specDefinitionId: number;

  @ApiProperty({ description: '스펙 값', example: 'Intel Core i7-1360P' })
  @IsString()
  value: string;

  @ApiPropertyOptional({ description: '수치 값' })
  @IsOptional()
  @IsNumber()
  numericValue?: number;
}

export class SetProductSpecsDto {
  @ApiProperty({ description: '스펙 목록', type: [ProductSpecItemDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ProductSpecItemDto)
  specs: ProductSpecItemDto[];
}
