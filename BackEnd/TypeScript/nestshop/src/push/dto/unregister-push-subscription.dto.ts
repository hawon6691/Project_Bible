import { ApiProperty } from '@nestjs/swagger';
import { IsString, Length } from 'class-validator';

export class UnregisterPushSubscriptionDto {
  @ApiProperty({ description: '해제할 endpoint' })
  @IsString()
  @Length(1, 1000)
  endpoint: string;
}
