import { IsOptional, IsString } from 'class-validator';

export class CarModelQueryDto {
  @IsOptional()
  @IsString()
  brand?: string;

  @IsOptional()
  @IsString()
  type?: string;
}
