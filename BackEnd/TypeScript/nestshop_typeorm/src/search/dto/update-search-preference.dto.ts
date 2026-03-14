import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean } from 'class-validator';

export class UpdateSearchPreferenceDto {
  @ApiProperty({ description: '검색어 자동 저장 여부' })
  @IsBoolean()
  searchHistoryEnabled: boolean;
}
