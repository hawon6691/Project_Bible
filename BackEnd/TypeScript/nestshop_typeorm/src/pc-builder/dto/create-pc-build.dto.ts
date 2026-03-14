import { Type } from 'class-transformer';
import { IsEnum, IsInt, IsOptional, IsString, Length, Min } from 'class-validator';
import { PcBuildPurpose } from '../entities/pc-build.entity';

export class CreatePcBuildDto {
  @IsString()
  @Length(1, 120)
  name: string;

  @IsOptional()
  @IsString()
  @Length(1, 500)
  description?: string;

  @IsEnum(PcBuildPurpose)
  purpose: PcBuildPurpose;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  budget?: number;
}
