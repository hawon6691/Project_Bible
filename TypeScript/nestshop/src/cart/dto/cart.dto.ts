import { IsInt, IsOptional, IsString, Min } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class AddCartItemDto {
  @ApiProperty({ description: '상품 ID' })
  @IsInt()
  productId: number;

  @ApiProperty({ description: '판매처 ID' })
  @IsInt()
  sellerId: number;

  @ApiProperty({ description: '수량', minimum: 1 })
  @IsInt()
  @Min(1)
  quantity: number;

  @ApiPropertyOptional({ description: '선택 옵션', example: '실버 / 512GB' })
  @IsOptional()
  @IsString()
  selectedOptions?: string;
}

export class UpdateCartQuantityDto {
  @ApiProperty({ description: '수량', minimum: 1 })
  @IsInt()
  @Min(1)
  quantity: number;
}
