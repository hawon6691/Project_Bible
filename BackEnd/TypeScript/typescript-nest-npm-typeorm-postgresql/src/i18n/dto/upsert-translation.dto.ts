import { IsString, Length, Matches } from 'class-validator';

export class UpsertTranslationDto {
  @IsString()
  @Length(2, 10)
  @Matches(/^[a-z]{2}(-[A-Z]{2})?$/)
  locale: string;

  @IsString()
  @Length(1, 50)
  namespace: string;

  @IsString()
  @Length(1, 120)
  key: string;

  @IsString()
  @Length(1, 2000)
  value: string;
}
