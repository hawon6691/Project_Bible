import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsNumber, Max, Min } from 'class-validator';

export class RecalculateTrustScoreDto {
  @ApiProperty({ description: '배송 정확도(0~100)' })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(100)
  deliveryAccuracy: number;

  @ApiProperty({ description: '가격 정확도(0~100)' })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(100)
  priceAccuracy: number;

  @ApiProperty({ description: '고객 평점 환산 점수(0~100)' })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(100)
  customerRating: number;

  @ApiProperty({ description: '응답 속도 점수(0~100)' })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(100)
  responseSpeed: number;

  @ApiProperty({ description: '반품률(0~100, 낮을수록 좋음)' })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(100)
  returnRate: number;
}
