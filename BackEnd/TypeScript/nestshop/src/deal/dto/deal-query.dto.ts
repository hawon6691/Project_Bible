import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsInt, IsOptional, Max, Min } from 'class-validator';

export class DealQueryDto {
  @ApiPropertyOptional({ description: '조회 개수', default: 20, minimum: 1, maximum: 100 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(100)
  limit: number = 20;

  @ApiPropertyOptional({ description: '활성 딜만 조회', default: true })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  activeOnly: boolean = true;
}
