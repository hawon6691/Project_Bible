import { IsString, Length } from 'class-validator';

export class CreateShortformCommentDto {
  @IsString()
  @Length(1, 500)
  content: string;
}
