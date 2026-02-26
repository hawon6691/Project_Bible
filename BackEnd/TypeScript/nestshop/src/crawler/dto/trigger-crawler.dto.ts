import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsInt, IsOptional, Min } from 'class-validator';

export class TriggerCrawlerDto {
  @ApiProperty({ description: '판매처 ID' })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  sellerId: number;

  @ApiPropertyOptional({ description: '특정 상품 ID (선택)' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  productId?: number;

  @ApiPropertyOptional({ description: '가격 수집 사용 여부 override', default: true })
  @IsOptional()
  @IsBoolean()
  collectPrice?: boolean;

  @ApiPropertyOptional({ description: '스펙 수집 사용 여부 override', default: true })
  @IsOptional()
  @IsBoolean()
  collectSpec?: boolean;

  @ApiPropertyOptional({ description: '이상치 검증 사용 여부 override', default: true })
  @IsOptional()
  @IsBoolean()
  detectAnomaly?: boolean;
}
