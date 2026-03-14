import { Type } from 'class-transformer';
import { IsEnum, IsInt, IsOptional, Min } from 'class-validator';
import { PcPartType } from '../entities/pc-build-part.entity';

export class AddPcBuildPartDto {
  @Type(() => Number)
  @IsInt()
  @Min(1)
  productId: number;

  @IsEnum(PcPartType)
  partType: PcPartType;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  sellerId?: number;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  quantity: number;
}
