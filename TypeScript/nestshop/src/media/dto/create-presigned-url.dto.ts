import { Type } from 'class-transformer';
import { IsInt, IsString, Length, Min } from 'class-validator';

export class CreatePresignedUrlDto {
  @IsString()
  @Length(1, 255)
  fileName: string;

  @IsString()
  @Length(1, 120)
  fileType: string;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  fileSize: number;
}
