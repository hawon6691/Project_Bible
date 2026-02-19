import { Type } from 'class-transformer';
import { IsInt, IsObject, IsOptional, IsString, Length, Min } from 'class-validator';

export class CreateAuctionDto {
  @IsString()
  @Length(1, 200)
  title: string;

  @IsString()
  @Length(1, 5000)
  description: string;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  categoryId: number;

  @IsOptional()
  @IsObject()
  specs?: Record<string, unknown>;

  @Type(() => Number)
  @IsInt()
  @Min(0)
  budget: number;
}
