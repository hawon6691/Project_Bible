import { IsInt, IsString, IsOptional, IsEnum, Min } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ShippingType } from '../entities/price-entry.entity';

export class CreatePriceEntryDto {
  @ApiProperty({ description: '판매처 ID' })
  @IsInt()
  sellerId: number;

  @ApiProperty({ description: '판매 가격' })
  @IsInt()
  @Min(0)
  price: number;

  @ApiPropertyOptional({ description: '배송비', default: 0 })
  @IsOptional()
  @IsInt()
  @Min(0)
  shippingCost?: number;

  @ApiPropertyOptional({ description: '배송 정보' })
  @IsOptional()
  @IsString()
  shippingInfo?: string;

  @ApiProperty({ description: '판매처 상품 URL' })
  @IsString()
  productUrl: string;

  @ApiPropertyOptional({ description: '배송비 유형', enum: ShippingType })
  @IsOptional()
  @IsEnum(ShippingType)
  shippingType?: ShippingType;
}

export class UpdatePriceEntryDto {
  @ApiPropertyOptional({ description: '판매 가격' })
  @IsOptional()
  @IsInt()
  @Min(0)
  price?: number;

  @ApiPropertyOptional({ description: '배송비' })
  @IsOptional()
  @IsInt()
  @Min(0)
  shippingCost?: number;

  @ApiPropertyOptional({ description: '배송 정보' })
  @IsOptional()
  @IsString()
  shippingInfo?: string;

  @ApiPropertyOptional({ description: '판매처 상품 URL' })
  @IsOptional()
  @IsString()
  productUrl?: string;

  @ApiPropertyOptional({ description: '구매 가능 여부' })
  @IsOptional()
  isAvailable?: boolean;
}

export class CreatePriceAlertDto {
  @ApiProperty({ description: '상품 ID' })
  @IsInt()
  productId: number;

  @ApiProperty({ description: '목표 가격' })
  @IsInt()
  @Min(1)
  targetPrice: number;
}
