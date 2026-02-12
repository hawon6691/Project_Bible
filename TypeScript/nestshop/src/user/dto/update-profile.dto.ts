import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, Length } from 'class-validator';

export class UpdateProfileDto {
  @ApiPropertyOptional({ example: '다나와마스터', description: '닉네임 (2~30자, 중복 불가)' })
  @IsOptional()
  @IsString()
  @Length(2, 30, { message: '닉네임은 2자 이상 30자 이하로 입력해주세요.' })
  nickname?: string;

  @ApiPropertyOptional({ example: '가격비교 전문가입니다.', description: '한 줄 소개글 (200자 이내)' })
  @IsOptional()
  @IsString()
  @Length(0, 200, { message: '소개글은 200자 이내로 입력해주세요.' })
  bio?: string;
}
