import { IsString, IsOptional, MaxLength } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CreateSellerDto {
  @ApiProperty({ description: '판매처명', example: '쿠팡' })
  @IsString()
  @MaxLength(100)
  name: string;

  @ApiProperty({ description: '판매처 URL', example: 'https://www.coupang.com' })
  @IsString()
  url: string;

  @ApiPropertyOptional({ description: '로고 URL' })
  @IsOptional()
  @IsString()
  logoUrl?: string;

  @ApiPropertyOptional({ description: '설명' })
  @IsOptional()
  @IsString()
  @MaxLength(200)
  description?: string;
}
