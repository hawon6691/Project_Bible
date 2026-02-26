import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsOptional, IsString, MaxLength, MinLength } from 'class-validator';
import { SocialProvider } from '../entities/social-account.entity';

export class SocialCallbackDto {
  @ApiProperty({ description: 'OAuth callback code' })
  @IsString()
  @MinLength(3)
  code: string;

  @ApiPropertyOptional({ description: 'state 값' })
  @IsOptional()
  @IsString()
  state?: string;

  @ApiPropertyOptional({ description: '개발/테스트용 provider email override' })
  @IsOptional()
  @IsString()
  @MaxLength(255)
  mockEmail?: string;

  @ApiPropertyOptional({ description: '개발/테스트용 provider name override' })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  mockName?: string;

  @ApiPropertyOptional({ description: '신규 소셜 유저 가입용 이름' })
  @IsOptional()
  @IsString()
  @MaxLength(50)
  signupName?: string;

  @ApiPropertyOptional({ description: '신규 소셜 유저 가입용 전화번호' })
  @IsOptional()
  @IsString()
  @MaxLength(20)
  signupPhone?: string;
}

export class CompleteSocialSignupDto {
  @ApiProperty({ description: '소셜 임시 가입 토큰' })
  @IsString()
  signupToken: string;

  @ApiProperty({ description: '이름' })
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ description: '전화번호' })
  @IsString()
  @MaxLength(20)
  phone: string;
}

export class LinkSocialAccountDto {
  @ApiProperty({ enum: SocialProvider, description: '연동할 소셜 제공자' })
  @IsEnum(SocialProvider)
  provider: SocialProvider;

  @ApiProperty({ description: 'OAuth callback code' })
  @IsString()
  @MinLength(3)
  code: string;

  @ApiPropertyOptional({ description: '개발/테스트용 provider email override' })
  @IsOptional()
  @IsString()
  @MaxLength(255)
  mockEmail?: string;

  @ApiPropertyOptional({ description: '개발/테스트용 provider name override' })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  mockName?: string;
}
