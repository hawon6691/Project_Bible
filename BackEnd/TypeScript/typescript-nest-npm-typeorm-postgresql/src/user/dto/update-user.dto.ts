import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, Length } from 'class-validator';
import { IsStrongPassword } from '../../common/validators/is-strong-password.validator';
import { IsKoreanPhone } from '../../common/validators/is-korean-phone.validator';

export class UpdateUserDto {
  @ApiPropertyOptional({ example: '홍길동', description: '이름 (2~20자)' })
  @IsOptional()
  @IsString()
  @Length(2, 20)
  name?: string;

  @ApiPropertyOptional({ example: '010-9876-5432' })
  @IsOptional()
  @IsKoreanPhone()
  phone?: string;

  @ApiPropertyOptional({ example: 'NewPassword1!', description: '새 비밀번호' })
  @IsOptional()
  @IsStrongPassword()
  password?: string;
}
