import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { IsOptional, IsString, Length } from 'class-validator';

export class AuctionListQueryDto extends PaginationRequestDto {
  @IsOptional()
  @IsString()
  @Length(1, 20)
  status?: string;

  @IsOptional()
  @IsString()
  categoryId?: string;
}
