import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional } from 'class-validator';

export class PushPreferenceDto {
  @ApiPropertyOptional({ description: '가격 알림 수신 여부' })
  @IsOptional()
  @IsBoolean()
  priceAlertEnabled?: boolean;

  @ApiPropertyOptional({ description: '주문 상태 알림 수신 여부' })
  @IsOptional()
  @IsBoolean()
  orderStatusEnabled?: boolean;

  @ApiPropertyOptional({ description: '채팅 메시지 알림 수신 여부' })
  @IsOptional()
  @IsBoolean()
  chatMessageEnabled?: boolean;

  @ApiPropertyOptional({ description: '특가 알림 수신 여부' })
  @IsOptional()
  @IsBoolean()
  dealEnabled?: boolean;
}
