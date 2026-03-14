import { IsString, IsOptional, IsInt, MaxLength, Min } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CreateCategoryDto {
  @ApiProperty({ description: '카테고리명', example: '노트북' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiPropertyOptional({ description: '부모 카테고리 ID', example: 1 })
  @IsOptional()
  @IsInt()
  @Min(1)
  parentId?: number;
}
