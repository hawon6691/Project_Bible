import { IsString, IsArray, MaxLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateOptionDto {
  @ApiProperty({ description: '옵션명', example: '색상' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ description: '옵션값 배열', example: ['실버', '그라파이트'] })
  @IsArray()
  @IsString({ each: true })
  values: string[];
}

export class UpdateOptionDto {
  @ApiProperty({ description: '옵션명', example: '색상' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ description: '옵션값 배열', example: ['실버', '그라파이트', '블랙'] })
  @IsArray()
  @IsString({ each: true })
  values: string[];
}
