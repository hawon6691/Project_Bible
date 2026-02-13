import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsInt, IsOptional, IsString, MaxLength, Min } from 'class-validator';

// 관리자 수동 포인트 지급 요청 DTO
export class AdminGrantPointDto {
  @ApiProperty({ description: '지급 대상 사용자 ID' })
  @IsInt()
  userId: number;

  @ApiProperty({ description: '지급 포인트' })
  @IsInt()
  @Min(1)
  amount: number;

  @ApiPropertyOptional({ description: '지급 사유' })
  @IsOptional()
  @IsString()
  @MaxLength(200)
  description?: string;
}
