import { ApiProperty } from '@nestjs/swagger';
import { IsString, Length } from 'class-validator';

export class CreateSearchHistoryDto {
  @ApiProperty({ description: '검색어', minLength: 1, maxLength: 100 })
  @IsString()
  @Length(1, 100)
  keyword: string;
}
