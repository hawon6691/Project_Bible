import { ApiProperty } from '@nestjs/swagger';
import { IsObject } from 'class-validator';

export class UpdateSearchWeightDto {
  @ApiProperty({
    description: '검색 필드 가중치',
    example: { name: 1.8, description: 1.1, spec: 1.3, popularity: 1.2 },
  })
  @IsObject()
  weights: Record<string, number>;
}
