import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { IsOptional, IsString, Length } from 'class-validator';

export class NewsListQueryDto extends PaginationRequestDto {
  @IsOptional()
  @IsString()
  @Length(1, 80)
  category?: string;
}
