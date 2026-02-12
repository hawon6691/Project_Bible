import {
  IsString, IsInt, IsOptional, IsArray, ValidateNested,
  MaxLength, Min, IsEnum, IsUrl,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { ProductStatus } from '../entities/product.entity';

export class CreateProductOptionDto {
  @ApiProperty({ description: '옵션명 (예: 색상, 사이즈)' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ description: '옵션값 목록', type: [String] })
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
  @IsString()
  @MaxLength(200)
  name: string;

  @ApiProperty({ description: '상품 설명' })
  @IsString()
  description: string;

  @ApiProperty({ description: '가격' })
  @IsInt()
  @Min(0)
  price: number;

  @ApiPropertyOptional({ description: '할인가' })
  @IsOptional()
  @IsInt()
  @Min(0)
  discountPrice?: number;

  @ApiProperty({ description: '재고' })
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
}
