import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsOptional, IsString, MaxLength } from 'class-validator';
import { PaginationRequestDto } from '../../common/dto/pagination.dto';
import { CommunityBoardType } from '../entities/community-post.entity';

export enum CommunityPostSort {
  LATEST = 'LATEST',
  VIEWS = 'VIEWS',
}

export class CommunityPostQueryDto extends PaginationRequestDto {
  @ApiPropertyOptional({ description: '게시판 타입 필터', enum: CommunityBoardType })
  @IsOptional()
  @IsEnum(CommunityBoardType)
  boardType?: CommunityBoardType;

  @ApiPropertyOptional({ description: '검색어(제목/본문)' })
  @IsOptional()
  @IsString()
  @MaxLength(50)
  keyword?: string;

  @ApiPropertyOptional({ description: '정렬 기준', enum: CommunityPostSort, default: CommunityPostSort.LATEST })
  @IsOptional()
  @IsEnum(CommunityPostSort)
  sort: CommunityPostSort = CommunityPostSort.LATEST;
}
