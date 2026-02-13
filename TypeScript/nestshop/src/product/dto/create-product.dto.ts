import {
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

  @ApiProperty({ description: '옵션값 배열', example: ['실버', '그라파이트'] })
  @IsArray()
  @IsString({ each: true })
  values: string[];
}

export class CreateProductImageDto {
  @ApiProperty({ description: '이미지 URL' })
  @IsString()
  url: string;

  @ApiPropertyOptional({ description: '대표 이미지 여부', default: false })
  @IsOptional()
  isMain?: boolean;

  @ApiPropertyOptional({ description: '정렬 순서', default: 0 })
  @IsOptional()
  @IsInt()
  @Min(0)
  sortOrder?: number;
}

export class CreateProductDto {
  @ApiProperty({ description: '상품명', example: '삼성 갤럭시북4 프로' })
  @IsString()
  @MaxLength(200)
  name: string;

  @ApiProperty({ description: '상품 설명' })
  @IsString()
  description: string;

  @ApiProperty({ description: '정가', example: 1590000 })
  @IsInt()
  @Min(0)
  price: number;

  @ApiPropertyOptional({ description: '할인가' })
  @IsOptional()
  @IsInt()
  @Min(0)
  discountPrice?: number;

  @ApiProperty({ description: '재고', example: 100 })
  @IsInt()
  @Min(0)
  stock: number;

  @ApiProperty({ description: '카테고리 ID', example: 1 })
  @IsInt()
  categoryId: number;

  @ApiPropertyOptional({ description: '상태', enum: ProductStatus })
  @IsOptional()
  @IsEnum(ProductStatus)
  status?: ProductStatus;

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
}
