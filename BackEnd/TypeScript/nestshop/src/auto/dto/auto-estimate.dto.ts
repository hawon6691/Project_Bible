import { Type } from 'class-transformer';
import { ArrayMinSize, IsArray, IsInt, Min } from 'class-validator';

export class AutoEstimateDto {
  @Type(() => Number)
  @IsInt()
  @Min(1)
  modelId: number;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  trimId: number;

  @IsArray()
  @Type(() => Number)
  @IsInt({ each: true })
  @Min(1, { each: true })
  optionIds: number[];
}
