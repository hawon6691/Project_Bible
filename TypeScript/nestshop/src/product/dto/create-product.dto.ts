import {
  IsString, IsInt, IsOptional, IsArray, ValidateNested,
  MaxLength, Min, IsEnum, IsUrl,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { ProductStatus } from '../entities/product.entity';

export class CreateProductOptionDto {
  @ApiProperty({ description: '옵션명 (예: 색상, 사이즈)' })
  IsString,
  IsInt,
  IsOptional,
  IsEnum,
  IsArray,
  ValidateNested,
  MaxLength,
  Min,
} from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ProductStatus } from '../entities/product.entity';

export class CreateProductOptionDto {
  @ApiProperty({ description: '옵션명', example: '색상' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ description: '옵션값 목록', type: [String] })
  @ApiProperty({ description: '옵션값 배열', example: ['실버', '그라파이트'] })
  @IsArray()
  @IsString({ each: true })
  values: string[];
}

export class CreateProductImageDto {
  @ApiProperty({ description: '이미지 URL' })
  @IsString()
  @MaxLength(500)
  url: string;

  @ApiPropertyOptional({ description: '대표 이미지 여부', default: false })
  @IsOptional()
  isMain?: boolean;

  @ApiPropertyOptional({ description: '정렬 순서', default: 0 })
  @IsOptional()
  @IsInt()
  sortOrder?: number;
}

export class CreateProductDto {
  @ApiProperty({ description: '상품명' })
  @Min(0)
  sortOrder?: number;
}

export class CreateProductSpecDto {
  @ApiProperty({ description: '스펙 정의 ID', example: 1 })
  @IsInt()
  specDefinitionId: number;

  @ApiProperty({ description: '스펙 값', example: 'Intel Core i7-1360P' })
  @IsString()
  value: string;

  @ApiPropertyOptional({ description: '수치 값 (비교/정렬용)' })
  @IsOptional()
  numericValue?: number;
}

export class CreateProductDto {
  @ApiProperty({ description: '상품명', example: '삼성 갤럭시북4 프로' })
  @IsString()
  @MaxLength(200)
  name: string;

  @ApiProperty({ description: '상품 설명' })
  @IsString()
  description: string;

  @ApiProperty({ description: '가격' })
  @ApiProperty({ description: '정가', example: 1590000 })
  @IsInt()
  @Min(0)
  price: number;

  @ApiPropertyOptional({ description: '할인가' })
  @IsOptional()
  @IsInt()
  @Min(0)
  discountPrice?: number;

  @ApiProperty({ description: '재고' })
  @ApiProperty({ description: '재고', example: 100 })
  @IsInt()
  @Min(0)
  stock: number;

  @ApiProperty({ description: '카테고리 ID' })
  @IsInt()
  categoryId: number;

  @ApiPropertyOptional({ description: '상태', enum: ProductStatus })
  @IsOptional()
  @IsEnum(ProductStatus)
  status?: ProductStatus;

  @ApiPropertyOptional({ description: '썸네일 URL' })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  @ApiProperty({ description: '카테고리 ID', example: 1 })
  @IsInt()
  categoryId: number;

  @ApiPropertyOptional({ description: '대표 이미지 URL' })
  @IsOptional()
  @IsString()
  thumbnailUrl?: string;

  @ApiPropertyOptional({ description: '옵션 목록', type: [CreateProductOptionDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => CreateProductOptionDto)
  options?: CreateProductOptionDto[];

  @ApiPropertyOptional({ description: '이미지 목록', type: [CreateProductImageDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => CreateProductImageDto)
  images?: CreateProductImageDto[];

  @ApiPropertyOptional({ description: '스펙 목록', type: [CreateProductSpecDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => CreateProductSpecDto)
  specs?: CreateProductSpecDto[];
}
