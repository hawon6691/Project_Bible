import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsInt, IsOptional, IsString, MaxLength, Min } from 'class-validator';

export class CreateCrawlerJobDto {
  @ApiProperty({ description: '판매처 ID' })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  sellerId: number;

  @ApiProperty({ description: '크롤러 작업명', maxLength: 100 })
  @IsString()
  @MaxLength(100)
  name: string;

  @ApiPropertyOptional({ description: '크론 표현식 (예: 0 */2 * * *)', maxLength: 100 })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  cronExpression?: string;

  @ApiPropertyOptional({ description: '가격 수집 사용 여부', default: true })
  @IsOptional()
  @IsBoolean()
  collectPrice?: boolean;

  @ApiPropertyOptional({ description: '스펙 수집 사용 여부', default: true })
  @IsOptional()
  @IsBoolean()
  collectSpec?: boolean;

  @ApiPropertyOptional({ description: '이상치 검증 사용 여부', default: true })
  @IsOptional()
  @IsBoolean()
  detectAnomaly?: boolean;

  @ApiPropertyOptional({ description: '작업 활성화 여부', default: true })
  @IsOptional()
  @IsBoolean()
  isActive?: boolean;
}
