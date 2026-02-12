import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Exclude, Expose } from 'class-transformer';

@Exclude()
export class UserResponseDto {
  @Expose()
  @ApiProperty()
  id: number;

  @Expose()
  @ApiProperty()
  email: string;

  @Expose()
  @ApiProperty()
  name: string;

  @Expose()
  @ApiProperty()
  phone: string;

  @Expose()
  @ApiProperty()
  role: string;

  @Expose()
  @ApiProperty()
  status: string;

  @Expose()
  @ApiProperty()
  emailVerified: boolean;

  @Expose()
  @ApiProperty()
  nickname: string;

  @Expose()
  @ApiPropertyOptional()
  bio: string | null;

  @Expose()
  @ApiPropertyOptional()
  profileImageUrl: string | null;

  @Expose()
  @ApiProperty()
  point: number;

  @Expose()
  @ApiProperty()
  createdAt: Date;
}
