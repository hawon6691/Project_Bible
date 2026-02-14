import { IsOptional, IsString, Length, Matches } from 'class-validator';

export class TranslationQueryDto {
  @IsOptional()
  @IsString()
  @Length(2, 10)
  @Matches(/^[a-z]{2}(-[A-Z]{2})?$/)
  locale?: string;

  @IsOptional()
  @IsString()
  @Length(1, 50)
  namespace?: string;

  @IsOptional()
  @IsString()
  @Length(1, 120)
  key?: string;
}
