import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsInt, IsOptional, IsString, Length, Max, Min } from 'class-validator';

export class CreateDealDto {
  @ApiProperty({ description: '상품 ID' })
  @Type(() => Number)
  @IsInt()
  productId: number;

  @ApiProperty({ description: '딜 제목', minLength: 1, maxLength: 120 })
  @IsString()
  @Length(1, 120)
  title: string;

  @ApiPropertyOptional({ description: '딜 설명' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiProperty({ description: '할인율(%)', minimum: 0, maximum: 100 })
  @Type(() => Number)
  @IsInt()
  @Min(0)
  @Max(100)
  discountRate: number;

  @ApiProperty({ description: '시작 시각(ISO 문자열)' })
  @Type(() => Date)
  startAt: Date;

  @ApiProperty({ description: '종료 시각(ISO 문자열)' })
  @Type(() => Date)
  endAt: Date;

  @ApiPropertyOptional({ description: '활성 여부', default: true })
  @IsOptional()
  @IsBoolean()
  isActive?: boolean;
}
