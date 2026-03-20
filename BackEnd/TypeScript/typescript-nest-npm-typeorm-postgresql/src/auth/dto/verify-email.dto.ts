import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsString, Length } from 'class-validator';

export class VerifyEmailDto {
  @ApiProperty({ example: 'user@example.com' })
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @ApiProperty({ example: '482931', description: '6자리 인증코드' })
  @IsString()
  @Length(6, 6, { message: '인증코드는 6자리입니다.' })
  @IsNotEmpty()
  code: string;
}
