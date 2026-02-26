import { Type } from 'class-transformer';
import { IsNumber, IsString, Length, Min } from 'class-validator';

export class UpsertExchangeRateDto {
  @IsString()
  @Length(3, 3)
  baseCurrency: string;

  @IsString()
  @Length(3, 3)
  targetCurrency: string;

  @Type(() => Number)
  @IsNumber({ maxDecimalPlaces: 8 })
  @Min(0.00000001)
  rate: number;
}
