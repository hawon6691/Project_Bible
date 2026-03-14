import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';
import { IsStrongPassword } from '../../common/validators/is-strong-password.validator';

export class ResetPasswordDto {
  @ApiProperty({ description: 'verify에서 발급받은 재설정 토큰' })
  @IsString()
  @IsNotEmpty()
  resetToken: string;

  @ApiProperty({ example: 'NewPassword1!', description: '새 비밀번호' })
  @IsStrongPassword()
  @IsNotEmpty()
  newPassword: string;
}
