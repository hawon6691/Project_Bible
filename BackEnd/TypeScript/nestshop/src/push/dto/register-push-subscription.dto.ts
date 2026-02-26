import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, Length } from 'class-validator';

export class RegisterPushSubscriptionDto {
  @ApiProperty({ description: 'Web Push endpoint' })
  @IsString()
  @Length(1, 1000)
  endpoint: string;

  @ApiProperty({ description: 'Web Push p256dh key' })
  @IsString()
  @Length(1, 255)
  p256dhKey: string;

  @ApiProperty({ description: 'Web Push auth key' })
  @IsString()
  @Length(1, 255)
  authKey: string;

  @ApiPropertyOptional({ description: '만료 시간(epoch ms)' })
  @IsOptional()
  @IsString()
  expirationTime?: string;
}
