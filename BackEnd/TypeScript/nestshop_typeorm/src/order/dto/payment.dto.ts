import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsInt, IsOptional, IsString, MaxLength, Min } from 'class-validator';
import { PaymentMethod } from '../entities/payment.entity';

// 결제 요청 입력값 DTO
export class CreatePaymentDto {
  @ApiProperty({ description: '주문 ID' })
  @IsInt()
  orderId: number;

  @ApiProperty({ enum: PaymentMethod, description: '결제 수단' })
  @IsEnum(PaymentMethod)
  method: PaymentMethod;

  // 최종 결제 금액과 일치 여부는 서비스에서 추가 검증한다.
  @ApiProperty({ description: '결제 금액' })
  @IsInt()
  @Min(0)
  amount: number;
}

// 환불 요청 입력값 DTO
export class RefundPaymentDto {
  @ApiPropertyOptional({ description: '환불 사유' })
  @IsOptional()
  @IsString()
  @MaxLength(200)
  reason?: string;
}
