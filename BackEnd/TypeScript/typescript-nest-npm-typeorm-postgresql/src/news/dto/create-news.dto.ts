import { Type } from 'class-transformer';
import { IsArray, IsInt, IsOptional, IsString, Length, Min } from 'class-validator';

export class CreateNewsDto {
  @IsString()
  @Length(1, 200)
  title: string;

  @IsString()
  @Length(1, 20000)
  content: string;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  categoryId: number;

  @IsOptional()
  @IsString()
  @Length(1, 500)
  thumbnailUrl?: string;

  @IsOptional()
  @IsArray()
  @Type(() => Number)
  @IsInt({ each: true })
  @Min(1, { each: true })
  productIds?: number[];
}
