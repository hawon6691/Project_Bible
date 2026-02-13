import { ApiProperty } from '@nestjs/swagger';
import { IsEnum, IsString, Length } from 'class-validator';
import { CommunityBoardType } from '../entities/community-post.entity';

export class CreateCommunityPostDto {
  @ApiProperty({ description: '게시판 타입', enum: CommunityBoardType })
  @IsEnum(CommunityBoardType)
  boardType: CommunityBoardType;

  @ApiProperty({ description: '제목', minLength: 1, maxLength: 120 })
  @IsString()
  @Length(1, 120)
  title: string;

  @ApiProperty({ description: '본문', minLength: 1, maxLength: 10000 })
  @IsString()
  @Length(1, 10000)
  content: string;
}
