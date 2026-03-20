import { IsString, Length } from 'class-validator';

export class RejectMappingDto {
  @IsString()
  @Length(1, 255)
  reason: string;
}
