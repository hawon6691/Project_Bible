import { ApiProperty } from '@nestjs/swagger';
import { IsInt, IsString, Max, MaxLength, Min } from 'class-validator';

export class CreateReviewDto {
  @ApiProperty({ description: '주문 ID' })
  @IsInt()
  orderId: number;

  @ApiProperty({ description: '평점 (1~5)' })
  @IsInt()
  @Min(1)
  @Max(5)
  rating: number;

  @ApiProperty({ description: '리뷰 내용' })
  @IsString()
  @MaxLength(3000)
  content: string;
}
