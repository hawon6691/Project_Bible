import { Type } from 'class-transformer';
import { IsInt, Max, Min } from 'class-validator';

export class UpdateReviewPolicyDto {
  @Type(() => Number)
  @IsInt()
  @Min(0)
  @Max(30)
  maxImageCount: number;

  @Type(() => Number)
  @IsInt()
  @Min(0)
  @Max(100000)
  pointAmount: number;
}
