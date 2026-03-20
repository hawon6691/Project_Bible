import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsString, Length } from 'class-validator';
import { IsStrongPassword } from '../../common/validators/is-strong-password.validator';
import { IsKoreanPhone } from '../../common/validators/is-korean-phone.validator';

export class SignupDto {
  @ApiProperty({ example: 'user@example.com', description: '이메일 (로그인 ID)' })
  @IsEmail({}, { message: '올바른 이메일 형식을 입력해주세요.' })
  @IsNotEmpty()
  email: string;

  @ApiProperty({ example: 'Password1!', description: '비밀번호 (8자 이상, 영문 대/소문자+숫자+특수문자)' })
  @IsStrongPassword()
  @IsNotEmpty()
  password: string;

  @ApiProperty({ example: '홍길동', description: '이름 (2~20자)' })
  @IsString()
  @Length(2, 20, { message: '이름은 2자 이상 20자 이하로 입력해주세요.' })
  @IsNotEmpty()
  name: string;

  @ApiProperty({ example: '010-1234-5678', description: '전화번호' })
  @IsKoreanPhone()
  @IsNotEmpty()
  phone: string;
}
