import { IsArray, IsString, Length, Matches } from 'class-validator';

export class SetAllowedExtensionsDto {
  @IsArray()
  @IsString({ each: true })
  @Length(1, 10, { each: true })
  @Matches(/^[a-z0-9]+$/, { each: true })
  extensions: string[];
}
