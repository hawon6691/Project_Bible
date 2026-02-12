import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class ProfileResponseDto {
  @ApiProperty()
  id: number;

  @ApiProperty()
  nickname: string;

  @ApiPropertyOptional()
  bio: string | null;

  @ApiPropertyOptional()
  profileImageUrl: string | null;

  @ApiProperty()
  createdAt: Date;
}
