import {
  Controller,
  Delete,
  Get,
  Param,
  ParseEnumPipe,
  Post,
  Body,
  HttpCode,
  HttpStatus,
  Query,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { Public } from '../common/decorators/public.decorator';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { SignupDto } from './dto/signup.dto';
import { LoginDto } from './dto/login.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { VerifyEmailDto } from './dto/verify-email.dto';
import { ResendVerificationDto } from './dto/resend-verification.dto';
import { RequestPasswordResetDto } from './dto/request-password-reset.dto';
import { VerifyResetCodeDto } from './dto/verify-reset-code.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import {
  CompleteSocialSignupDto,
  LinkSocialAccountDto,
  SocialCallbackDto,
} from './dto/social-auth.dto';
import { SocialProvider } from './entities/social-account.entity';

@ApiTags('Auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Post('signup')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: '회원가입 (인증 메일 자동 발송)' })
  signup(@Body() dto: SignupDto) {
    return this.authService.signup(dto);
  }

  @Public()
  @Post('verify-email')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '이메일 인증 확인' })
  verifyEmail(@Body() dto: VerifyEmailDto) {
    return this.authService.verifyEmail(dto);
  }

  @Public()
  @Post('resend-verification')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '인증 메일 재발송' })
  resendVerification(@Body() dto: ResendVerificationDto) {
    return this.authService.resendVerification(dto);
  }

  @Public()
  @Post('login')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '로그인' })
  login(@Body() dto: LoginDto) {
    return this.authService.login(dto);
  }

  @Post('logout')
  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @ApiOperation({ summary: '로그아웃' })
  logout(@CurrentUser() user: JwtPayload) {
    return this.authService.logout(user.sub);
  }

  @Public()
  @Post('refresh')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '토큰 갱신' })
  refresh(@Body() dto: RefreshTokenDto) {
    return this.authService.refresh(dto.refreshToken);
  }

  @Public()
  @Post('password-reset/request')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '비밀번호 재설정 요청 (이메일 + 전화번호 확인)' })
  requestPasswordReset(@Body() dto: RequestPasswordResetDto) {
    return this.authService.requestPasswordReset(dto);
  }

  @Public()
  @Post('password-reset/verify')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '비밀번호 재설정 인증코드 확인' })
  verifyResetCode(@Body() dto: VerifyResetCodeDto) {
    return this.authService.verifyResetCode(dto);
  }

  @Public()
  @Post('password-reset/confirm')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '새 비밀번호 설정' })
  resetPassword(@Body() dto: ResetPasswordDto) {
    return this.authService.resetPassword(dto);
  }

  // AUTH-11: 소셜 로그인 요청 URL 반환
  @Public()
  @Get(':provider')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '소셜 로그인 요청 URL 생성' })
  getSocialAuthUrl(
    @Param('provider', new ParseEnumPipe(SocialProvider)) provider: SocialProvider,
    @Query('state') state?: string,
    @Query('redirectUri') redirectUri?: string,
  ) {
    return this.authService.getSocialAuthUrl(provider, state, redirectUri);
  }

  // AUTH-12: 소셜 콜백 처리
  @Public()
  @Post(':provider/callback')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '소셜 로그인 콜백 처리' })
  socialCallback(
    @Param('provider', new ParseEnumPipe(SocialProvider)) provider: SocialProvider,
    @Body() dto: SocialCallbackDto,
  ) {
    return this.authService.socialCallback(provider, dto);
  }

  // AUTH-15: 신규 소셜 유저 추가 정보 입력 후 가입 완료
  @Public()
  @Post('social/signup-complete')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '신규 소셜 유저 가입 완료' })
  completeSocialSignup(@Body() dto: CompleteSocialSignupDto) {
    return this.authService.completeSocialSignup(dto);
  }

  // AUTH-13: 소셜 계정 연동
  @Post('social/link')
  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @ApiOperation({ summary: '소셜 계정 연동' })
  linkSocialAccount(@CurrentUser() user: JwtPayload, @Body() dto: LinkSocialAccountDto) {
    return this.authService.linkSocialAccount(user.sub, dto);
  }

  // AUTH-14: 소셜 계정 연동 해제
  @Delete('social/:provider')
  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @ApiOperation({ summary: '소셜 계정 연동 해제' })
  unlinkSocialAccount(
    @CurrentUser() user: JwtPayload,
    @Param('provider', new ParseEnumPipe(SocialProvider)) provider: SocialProvider,
  ) {
    return this.authService.unlinkSocialAccount(user.sub, provider);
  }
}
