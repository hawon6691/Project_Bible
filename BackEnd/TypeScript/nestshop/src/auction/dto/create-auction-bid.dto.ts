import { Type } from 'class-transformer';
import { IsInt, IsOptional, IsString, Length, Min } from 'class-validator';

export class CreateAuctionBidDto {
  @Type(() => Number)
  @IsInt()
  @Min(0)
  price: number;

  @IsOptional()
  @IsString()
  @Length(1, 500)
  description?: string;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  deliveryDays: number;
}
