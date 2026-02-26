import { Injectable, HttpStatus, Logger } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User, UserStatus } from '../user/entities/user.entity';
import { EmailVerification, VerificationType } from './entities/email-verification.entity';
import { MailService } from '../mail/mail.service';
import { BusinessException } from '../common/exceptions/business.exception';
import { hashPassword, comparePassword } from '../common/utils/hash.util';
import { addMinutes, isExpired } from '../common/utils/date.util';
import { SignupDto } from './dto/signup.dto';
import { LoginDto } from './dto/login.dto';
import { VerifyEmailDto } from './dto/verify-email.dto';
import { ResendVerificationDto } from './dto/resend-verification.dto';
import { RequestPasswordResetDto } from './dto/request-password-reset.dto';
import { VerifyResetCodeDto } from './dto/verify-reset-code.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import { TokenResponseDto } from './dto/token-response.dto';
import {
  CompleteSocialSignupDto,
  LinkSocialAccountDto,
  SocialCallbackDto,
} from './dto/social-auth.dto';
import { SocialAccount, SocialProvider } from './entities/social-account.entity';

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(EmailVerification)
    private verificationRepository: Repository<EmailVerification>,
    @InjectRepository(SocialAccount)
    private socialAccountRepository: Repository<SocialAccount>,
    private jwtService: JwtService,
    private configService: ConfigService,
    private mailService: MailService,
  ) {}

  // ─── AUTH-01: 회원가입 ───
  async signup(dto: SignupDto) {
    // 이메일 중복 확인
    const existingUser = await this.userRepository.findOne({
      where: { email: dto.email },
    });
    if (existingUser) {
      throw new BusinessException('AUTH_EMAIL_ALREADY_EXISTS', HttpStatus.CONFLICT);
    }

    // 비밀번호 해싱
    const hashedPassword = await hashPassword(dto.password);

    // 닉네임 자동 생성 (이메일 앞부분 + 랜덤 숫자)
    const emailPrefix = dto.email.split('@')[0];
    const randomSuffix = Math.floor(Math.random() * 10000);
    const nickname = `${emailPrefix}${randomSuffix}`;

    // 사용자 생성
    const user = this.userRepository.create({
      email: dto.email,
      password: hashedPassword,
      name: dto.name,
      phone: dto.phone.replace(/[\s-]/g, ''),
      nickname,
      emailVerified: false,
    });
    const savedUser = await this.userRepository.save(user);

    // 인증코드 생성 및 메일 발송
    await this.createAndSendVerification(savedUser, VerificationType.SIGNUP);

    return {
      id: savedUser.id,
      email: savedUser.email,
      name: savedUser.name,
      message: '인증 메일이 발송되었습니다. 이메일을 확인해주세요.',
    };
  }

  // ─── AUTH-02: 로그인 ───
  async login(dto: LoginDto): Promise<TokenResponseDto> {
    const user = await this.userRepository.findOne({
      where: { email: dto.email },
    });

    if (!user || !user.password) {
      throw new BusinessException('AUTH_INVALID_CREDENTIALS', HttpStatus.UNAUTHORIZED);
    }

    const isPasswordValid = await comparePassword(dto.password, user.password);
    if (!isPasswordValid) {
      throw new BusinessException('AUTH_INVALID_CREDENTIALS', HttpStatus.UNAUTHORIZED);
    }

    // 이메일 인증 확인
    if (!user.emailVerified) {
      throw new BusinessException('AUTH_EMAIL_NOT_VERIFIED', HttpStatus.FORBIDDEN);
    }

    // 차단 확인
    if (user.status === UserStatus.BLOCKED) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    // JWT 발급
    const tokens = await this.generateTokens(user);

    // Refresh Token 저장
    const hashedRefreshToken = await hashPassword(tokens.refreshToken);
    await this.userRepository.update(user.id, { refreshToken: hashedRefreshToken });

    return tokens;
  }

  // ─── AUTH-03: 로그아웃 ───
  async logout(userId: number): Promise<{ message: string }> {
    await this.userRepository.update(userId, { refreshToken: null });
    return { message: '로그아웃 되었습니다.' };
  }

  // ─── AUTH-04: 토큰 갱신 ───
  async refresh(refreshToken: string): Promise<TokenResponseDto> {
    let payload: { sub: number; email: string; role: string };

    try {
      payload = this.jwtService.verify(refreshToken, {
        secret: this.configService.get<string>('JWT_REFRESH_SECRET'),
      });
    } catch {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED);
    }

    const user = await this.userRepository.findOne({ where: { id: payload.sub } });
    if (!user || !user.refreshToken) {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED);
    }

    // 저장된 Refresh Token과 비교
    const isValid = await comparePassword(refreshToken, user.refreshToken);
    if (!isValid) {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED);
    }

    // 새 토큰 발급
    const tokens = await this.generateTokens(user);
    const hashedRefreshToken = await hashPassword(tokens.refreshToken);
    await this.userRepository.update(user.id, { refreshToken: hashedRefreshToken });

    return tokens;
  }

  // ─── AUTH-06: 이메일 인증 확인 ───
  async verifyEmail(dto: VerifyEmailDto) {
    const user = await this.userRepository.findOne({ where: { email: dto.email } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (user.emailVerified) {
      throw new BusinessException('AUTH_EMAIL_ALREADY_EXISTS', HttpStatus.CONFLICT, '이미 인증된 이메일입니다.');
    }

    const verification = await this.verificationRepository.findOne({
      where: { userId: user.id, type: VerificationType.SIGNUP, isUsed: false },
      order: { createdAt: 'DESC' },
    });

    if (!verification) {
      throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.BAD_REQUEST);
    }

    // 시도 횟수 확인
    if (verification.attemptCount >= 5) {
      throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.TOO_MANY_REQUESTS, '인증 시도 횟수를 초과했습니다.');
    }

    // 만료 확인
    if (isExpired(verification.expiresAt)) {
      throw new BusinessException('AUTH_VERIFICATION_CODE_EXPIRED', HttpStatus.GONE);
    }

    // 코드 확인
    if (verification.code !== dto.code) {
      await this.verificationRepository.update(verification.id, {
        attemptCount: verification.attemptCount + 1,
      });
      throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.BAD_REQUEST);
    }

    // 인증 완료
    await this.verificationRepository.update(verification.id, { isUsed: true });
    await this.userRepository.update(user.id, {
      emailVerified: true,
      emailVerifiedAt: new Date(),
    });

    return { message: '이메일 인증이 완료되었습니다.', verified: true };
  }

  // ─── AUTH-07: 인증 메일 재발송 ───
  async resendVerification(dto: ResendVerificationDto) {
    const user = await this.userRepository.findOne({ where: { email: dto.email } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (user.emailVerified) {
      throw new BusinessException('AUTH_EMAIL_ALREADY_EXISTS', HttpStatus.CONFLICT, '이미 인증된 이메일입니다.');
    }

    // Rate Limit: 1분 이내 재발송 방지
    const recentVerification = await this.verificationRepository.findOne({
      where: { userId: user.id, type: VerificationType.SIGNUP },
      order: { createdAt: 'DESC' },
    });

    if (recentVerification) {
      const oneMinuteAgo = new Date(Date.now() - 60 * 1000);
      if (recentVerification.createdAt > oneMinuteAgo) {
        throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.TOO_MANY_REQUESTS, '1분 후 다시 시도해주세요.');
      }
    }

    await this.createAndSendVerification(user, VerificationType.SIGNUP);

    return { message: '인증 메일이 재발송되었습니다.' };
  }

  // ─── AUTH-08: 비밀번호 재설정 요청 ───
  async requestPasswordReset(dto: RequestPasswordResetDto) {
    const user = await this.userRepository.findOne({ where: { email: dto.email } });

    // 보안: 이메일/전화번호 불일치 시에도 동일 응답
    if (!user || user.phone !== dto.phone.replace(/[\s-]/g, '')) {
      // 일정 지연 후 동일한 응답 반환 (타이밍 공격 방지)
      await new Promise((resolve) => setTimeout(resolve, 500));
      return { message: '비밀번호 재설정 인증 메일이 발송되었습니다.' };
    }

    // Rate Limit
    const recentVerification = await this.verificationRepository.findOne({
      where: { userId: user.id, type: VerificationType.PASSWORD_RESET },
      order: { createdAt: 'DESC' },
    });

    if (recentVerification) {
      const oneMinuteAgo = new Date(Date.now() - 60 * 1000);
      if (recentVerification.createdAt > oneMinuteAgo) {
        throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.TOO_MANY_REQUESTS, '1분 후 다시 시도해주세요.');
      }
    }

    await this.createAndSendVerification(user, VerificationType.PASSWORD_RESET);

    return { message: '비밀번호 재설정 인증 메일이 발송되었습니다.' };
  }

  // ─── AUTH-09: 비밀번호 재설정 인증코드 확인 ───
  async verifyResetCode(dto: VerifyResetCodeDto) {
    const user = await this.userRepository.findOne({ where: { email: dto.email } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const verification = await this.verificationRepository.findOne({
      where: { userId: user.id, type: VerificationType.PASSWORD_RESET, isUsed: false },
      order: { createdAt: 'DESC' },
    });

    if (!verification) {
      throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.BAD_REQUEST);
    }

    if (verification.attemptCount >= 5) {
      throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.TOO_MANY_REQUESTS, '인증 시도 횟수를 초과했습니다.');
    }

    if (isExpired(verification.expiresAt)) {
      throw new BusinessException('AUTH_VERIFICATION_CODE_EXPIRED', HttpStatus.GONE);
    }

    if (verification.code !== dto.code) {
      await this.verificationRepository.update(verification.id, {
        attemptCount: verification.attemptCount + 1,
      });
      throw new BusinessException('AUTH_VERIFICATION_CODE_INVALID', HttpStatus.BAD_REQUEST);
    }

    // 인증 성공 → 재설정 토큰 발급 (5분 유효)
    await this.verificationRepository.update(verification.id, { isUsed: true });

    const resetToken = this.jwtService.sign(
      { sub: user.id, email: user.email, type: 'password_reset' },
      {
        secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
        expiresIn: '5m',
      },
    );

    return { resetToken };
  }

  // ─── AUTH-10: 비밀번호 재설정 ───
  async resetPassword(dto: ResetPasswordDto) {
    let payload: { sub: number; type: string };

    try {
      payload = this.jwtService.verify(dto.resetToken, {
        secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
      });
    } catch {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED, '유효하지 않거나 만료된 재설정 토큰입니다.');
    }

    if (payload.type !== 'password_reset') {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED);
    }

    const user = await this.userRepository.findOne({ where: { id: payload.sub } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 기존 비밀번호와 동일 여부 확인
    if (user.password) {
      const isSame = await comparePassword(dto.newPassword, user.password);
      if (isSame) {
        throw new BusinessException('AUTH_INVALID_CREDENTIALS', HttpStatus.BAD_REQUEST, '기존 비밀번호와 동일한 비밀번호입니다.');
      }
    }

    // 비밀번호 변경 + 모든 세션 무효화
    const hashedPassword = await hashPassword(dto.newPassword);
    await this.userRepository.update(user.id, {
      password: hashedPassword,
      refreshToken: null,
    });

    return { message: '비밀번호가 성공적으로 변경되었습니다.' };
  }

  // ─── AUTH-11: 소셜 로그인 URL 생성 ───
  async getSocialAuthUrl(provider: SocialProvider, state?: string, redirectUri?: string) {
    const clientId = this.configService.get<string>(`${provider.toUpperCase()}_CLIENT_ID`) ?? 'dev-client-id';
    const resolvedRedirect =
      redirectUri ??
      this.configService.get<string>('SOCIAL_REDIRECT_URI') ??
      `http://localhost:3000/api/v1/auth/${provider}/callback`;

    // 실제 인가 URL은 provider별 SDK/전략으로 대체 가능하며, 현재는 API 계약을 우선 제공한다.
    const authorizationUrl =
      `https://auth.${provider}.example/authorize?client_id=${encodeURIComponent(clientId)}` +
      `&redirect_uri=${encodeURIComponent(resolvedRedirect)}` +
      `&response_type=code` +
      `${state ? `&state=${encodeURIComponent(state)}` : ''}`;

    return {
      provider,
      authorizationUrl,
      state: state ?? null,
      redirectUri: resolvedRedirect,
    };
  }

  // ─── AUTH-12: 소셜 콜백 처리 ───
  async socialCallback(provider: SocialProvider, dto: SocialCallbackDto) {
    const profile = await this.resolveSocialProfile(provider, dto.code, dto.mockEmail, dto.mockName);

    let social = await this.socialAccountRepository.findOne({
      where: { provider, providerUserId: profile.providerUserId },
    });

    if (social) {
      const user = await this.userRepository.findOne({ where: { id: social.userId } });
      if (!user) {
        throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      const tokens = await this.generateTokens(user);
      await this.userRepository.update(user.id, { refreshToken: await hashPassword(tokens.refreshToken) });
      return {
        ...tokens,
        provider,
        isNewUser: false,
      };
    }

    if (profile.email) {
      const existingUser = await this.userRepository.findOne({ where: { email: profile.email } });
      if (existingUser) {
        social = this.socialAccountRepository.create({
          userId: existingUser.id,
          provider,
          providerUserId: profile.providerUserId,
          providerEmail: profile.email,
          providerName: profile.name,
        });
        await this.socialAccountRepository.save(social);

        const tokens = await this.generateTokens(existingUser);
        await this.userRepository.update(existingUser.id, { refreshToken: await hashPassword(tokens.refreshToken) });
        return {
          ...tokens,
          provider,
          isNewUser: false,
        };
      }
    }

    if (!dto.signupName || !dto.signupPhone) {
      const signupToken = await this.jwtService.signAsync(
        {
          type: 'social_signup',
          provider,
          providerUserId: profile.providerUserId,
          email: profile.email,
          name: profile.name,
        },
        {
          secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
          expiresIn: '10m',
        },
      );

      return {
        provider,
        isNewUser: true,
        requiresSignup: true,
        signupToken,
        message: '추가 정보 입력 후 가입을 완료해주세요.',
      };
    }

    return this.completeSocialSignup({
      signupToken: await this.jwtService.signAsync(
        {
          type: 'social_signup',
          provider,
          providerUserId: profile.providerUserId,
          email: profile.email,
          name: profile.name,
        },
        {
          secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
          expiresIn: '10m',
        },
      ),
      name: dto.signupName,
      phone: dto.signupPhone,
    });
  }

  // ─── AUTH-15: 신규 소셜 유저 가입 완료 ───
  async completeSocialSignup(dto: CompleteSocialSignupDto) {
    let payload: {
      type: string;
      provider: SocialProvider;
      providerUserId: string;
      email: string | null;
      name: string | null;
    };

    try {
      payload = this.jwtService.verify(dto.signupToken, {
        secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
      });
    } catch {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED, '유효하지 않은 소셜 가입 토큰입니다.');
    }

    if (payload.type !== 'social_signup') {
      throw new BusinessException('AUTH_TOKEN_INVALID', HttpStatus.UNAUTHORIZED);
    }

    const existingSocial = await this.socialAccountRepository.findOne({
      where: {
        provider: payload.provider,
        providerUserId: payload.providerUserId,
      },
    });
    if (existingSocial) {
      throw new BusinessException('AUTH_INVALID_CREDENTIALS', HttpStatus.CONFLICT, '이미 가입된 소셜 계정입니다.');
    }

    const email = payload.email ?? `${payload.provider}_${payload.providerUserId}@social.local`;
    const existingUser = await this.userRepository.findOne({ where: { email } });
    if (existingUser) {
      throw new BusinessException('AUTH_EMAIL_ALREADY_EXISTS', HttpStatus.CONFLICT);
    }

    const nickname = await this.generateUniqueNickname(email.split('@')[0]);
    const user = this.userRepository.create({
      email,
      password: null,
      name: dto.name,
      phone: dto.phone.replace(/[\s-]/g, ''),
      nickname,
      emailVerified: true,
      emailVerifiedAt: new Date(),
    });
    const savedUser = await this.userRepository.save(user);

    const social = this.socialAccountRepository.create({
      userId: savedUser.id,
      provider: payload.provider,
      providerUserId: payload.providerUserId,
      providerEmail: payload.email,
      providerName: payload.name ?? dto.name,
    });
    await this.socialAccountRepository.save(social);

    const tokens = await this.generateTokens(savedUser);
    await this.userRepository.update(savedUser.id, { refreshToken: await hashPassword(tokens.refreshToken) });

    return {
      ...tokens,
      provider: payload.provider,
      isNewUser: true,
    };
  }

  // ─── AUTH-13: 소셜 계정 연동 ───
  async linkSocialAccount(userId: number, dto: LinkSocialAccountDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const profile = await this.resolveSocialProfile(dto.provider, dto.code, dto.mockEmail, dto.mockName);

    const taken = await this.socialAccountRepository.findOne({
      where: { provider: dto.provider, providerUserId: profile.providerUserId },
    });
    if (taken && taken.userId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.CONFLICT, '다른 계정에 이미 연결된 소셜 계정입니다.');
    }

    const already = await this.socialAccountRepository.findOne({
      where: { userId, provider: dto.provider },
    });
    if (already) {
      return { success: true, message: '이미 연동된 소셜 계정입니다.' };
    }

    const social = this.socialAccountRepository.create({
      userId,
      provider: dto.provider,
      providerUserId: profile.providerUserId,
      providerEmail: profile.email,
      providerName: profile.name,
    });
    await this.socialAccountRepository.save(social);
    return { success: true, message: '소셜 계정이 연동되었습니다.' };
  }

  // ─── AUTH-14: 소셜 계정 연동 해제 ───
  async unlinkSocialAccount(userId: number, provider: SocialProvider) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const social = await this.socialAccountRepository.findOne({ where: { userId, provider } });
    if (!social) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '연동된 소셜 계정이 없습니다.');
    }

    const socialCount = await this.socialAccountRepository.count({ where: { userId } });
    const hasPassword = !!user.password;
    if (!hasPassword && socialCount <= 1) {
      throw new BusinessException(
        'AUTH_FORBIDDEN',
        HttpStatus.BAD_REQUEST,
        '일반 로그인 수단이 없어 소셜 연동을 해제할 수 없습니다.',
      );
    }

    await this.socialAccountRepository.softDelete({ id: social.id });
    return { success: true, message: '소셜 계정 연동이 해제되었습니다.' };
  }

  // ─── 헬퍼: 토큰 생성 ───
  private async generateTokens(user: User): Promise<TokenResponseDto> {
    const payload = { sub: user.id, email: user.email, role: user.role };

    const [accessToken, refreshToken] = await Promise.all([
      this.jwtService.signAsync(payload, {
        secret: this.configService.get<string>('JWT_ACCESS_SECRET'),
        expiresIn: 900, // 15분
      }),
      this.jwtService.signAsync(payload, {
        secret: this.configService.get<string>('JWT_REFRESH_SECRET'),
        expiresIn: 604800, // 7일
      }),
    ]);

    return {
      accessToken,
      refreshToken,
      expiresIn: 900, // 15분 = 900초
    };
  }

  // ─── 헬퍼: 인증코드 생성 및 발송 ───
  private async createAndSendVerification(
    user: User,
    type: VerificationType,
  ): Promise<void> {
    // 6자리 랜덤 인증코드
    const code = Math.floor(100000 + Math.random() * 900000).toString();

    const verification = this.verificationRepository.create({
      userId: user.id,
      type,
      code,
      expiresAt: addMinutes(10),
    });
    await this.verificationRepository.save(verification);

    // 메일 발송
    if (type === VerificationType.SIGNUP) {
      await this.mailService.sendVerificationEmail(user.email, code, user.name);
    } else {
      await this.mailService.sendPasswordResetEmail(user.email, code, user.name);
    }
  }

  private async resolveSocialProfile(
    provider: SocialProvider,
    code: string,
    mockEmail?: string,
    mockName?: string,
  ) {
    const normalized = code.trim();
    if (!normalized) {
      throw new BusinessException('AUTH_INVALID_CREDENTIALS', HttpStatus.BAD_REQUEST, '유효한 소셜 code가 필요합니다.');
    }

    // 외부 OAuth API 연동 전 단계에서는 code 기반 식별자를 생성해 안정적으로 재로그인을 보장한다.
    const providerUserId = `${provider}_${normalized.slice(0, 40)}`;
    const email = mockEmail?.trim() || `${provider}_${normalized.slice(0, 20)}@social.local`;
    const name = mockName?.trim() || `${provider}_user`;

    return { providerUserId, email, name };
  }

  private async generateUniqueNickname(base: string) {
    const prefix = base.replace(/[^a-zA-Z0-9가-힣]/g, '').slice(0, 20) || 'user';

    for (let i = 0; i < 20; i++) {
      const suffix = Math.floor(Math.random() * 100000)
        .toString()
        .padStart(5, '0');
      const candidate = `${prefix}${suffix}`.slice(0, 30);
      const exists = await this.userRepository.findOne({ where: { nickname: candidate } });
      if (!exists) return candidate;
    }

    throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT, '닉네임 생성에 실패했습니다.');
  }
}
