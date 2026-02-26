import { IsInt, IsOptional, IsString, IsArray, IsBoolean, ValidateNested, Min } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class OrderItemDto {
  @ApiProperty({ description: '상품 ID' })
  @IsInt()
  productId: number;

  @ApiProperty({ description: '판매처 ID' })
  @IsInt()
  sellerId: number;

  @ApiProperty({ description: '수량' })
  @IsInt()
  @Min(1)
  quantity: number;

  @ApiPropertyOptional({ description: '선택 옵션' })
  @IsOptional()
  @IsString()
  selectedOptions?: string;
}

export class CreateOrderDto {
  @ApiProperty({ description: '배송지 ID' })
  @IsInt()
  addressId: number;

  @ApiProperty({ description: '주문 상품 목록', type: [OrderItemDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => OrderItemDto)
  items: OrderItemDto[];

  @ApiPropertyOptional({ description: '장바구니에서 주문 여부' })
  @IsOptional()
  @IsBoolean()
  fromCart?: boolean;

  @ApiPropertyOptional({ description: '삭제할 장바구니 항목 ID' })
  @IsOptional()
  @IsArray()
  @IsInt({ each: true })
  cartItemIds?: number[];

  @ApiPropertyOptional({ description: '사용 포인트', default: 0 })
  @IsOptional()
  @IsInt()
  @Min(0)
  usePoint?: number;

  @ApiPropertyOptional({ description: '배송 메모' })
  @IsOptional()
  @IsString()
  memo?: string;
}
