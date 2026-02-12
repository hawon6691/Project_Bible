import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty } from 'class-validator';
import { IsKoreanPhone } from '../../common/validators/is-korean-phone.validator';

export class RequestPasswordResetDto {
  @ApiProperty({ example: 'user@example.com' })
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @ApiProperty({ example: '010-1234-5678', description: '가입 시 등록한 전화번호' })
  @IsKoreanPhone()
  @IsNotEmpty()
  phone: string;
}
