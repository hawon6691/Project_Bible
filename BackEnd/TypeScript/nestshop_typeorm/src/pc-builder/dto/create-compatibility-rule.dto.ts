import { Type } from 'class-transformer';
import { IsBoolean, IsEnum, IsObject, IsOptional, IsString, Length, ValidateNested } from 'class-validator';
import { CompatibilitySeverity } from '../entities/pc-compatibility-rule.entity';
import { PcPartType } from '../entities/pc-build-part.entity';

class RuleMetadataDto {
  @IsOptional()
  @IsBoolean()
  required?: boolean;
}

export class CreateCompatibilityRuleDto {
  @IsEnum(PcPartType)
  partType: PcPartType;

  @IsOptional()
  @IsEnum(PcPartType)
  targetPartType?: PcPartType;

  @IsString()
  @Length(1, 100)
  title: string;

  @IsString()
  @Length(1, 500)
  description: string;

  @IsEnum(CompatibilitySeverity)
  severity: CompatibilitySeverity;

  @IsOptional()
  @IsBoolean()
  enabled?: boolean;

  @IsOptional()
  @IsObject()
  @ValidateNested()
  @Type(() => RuleMetadataDto)
  metadata?: RuleMetadataDto;
}
