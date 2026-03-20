<?php

namespace App\Modules\Auth\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\User;
use App\Modules\Auth\Enums\AuthCodePurpose;
use App\Modules\Auth\Models\AuthCode;
use App\Modules\Auth\Models\PasswordResetToken;
use App\Modules\Auth\Models\RefreshToken;
use App\Modules\Auth\Models\SocialAccount;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class AuthService
{
    public function __construct(
        private readonly JwtService $jwtService,
    ) {}

    public function signup(array $payload): array
    {
        return DB::transaction(function () use ($payload): array {
            $user = User::query()->create([
                'email' => $payload['email'],
                'password' => Hash::make($payload['password']),
                'name' => $payload['name'],
                'phone' => $payload['phone'],
                'role' => 'USER',
                'status' => 'ACTIVE',
            ]);

            $code = $this->issueAuthCode($user, AuthCodePurpose::EMAIL_VERIFICATION);
            $this->sendEmailVerificationMail($user->email, $code);

            return [
                'id' => $user->id,
                'email' => $user->email,
                'name' => $user->name,
                'message' => '회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.',
            ];
        });
    }

    public function verifyEmail(string $email, string $code): array
    {
        $record = $this->findValidCode($email, AuthCodePurpose::EMAIL_VERIFICATION, $code);
        $user = User::query()->where('email', $email)->firstOrFail();

        $record->update(['verified_at' => now()]);
        $user->forceFill([
            'email_verified_at' => now(),
            'status' => 'ACTIVE',
        ])->save();

        return [
            'message' => '이메일 인증이 완료되었습니다.',
            'verified' => true,
        ];
    }

    public function resendVerification(string $email): array
    {
        $user = User::query()->where('email', $email)->first();

        if (! $user) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        if ($user->email_verified_at) {
            throw new BusinessException('이미 인증된 이메일입니다.', 'EMAIL_ALREADY_VERIFIED', Response::HTTP_BAD_REQUEST);
        }

        AuthCode::query()
            ->where('email', $email)
            ->where('purpose', AuthCodePurpose::EMAIL_VERIFICATION)
            ->whereNull('verified_at')
            ->delete();

        $code = $this->issueAuthCode($user, AuthCodePurpose::EMAIL_VERIFICATION);
        $this->sendEmailVerificationMail($email, $code);

        return ['message' => '인증 메일이 재발송되었습니다.'];
    }

    public function login(string $email, string $password): array
    {
        $user = User::query()->where('email', $email)->first();

        if (! $user || ! Hash::check($password, $user->password)) {
            throw new BusinessException('이메일 또는 비밀번호가 올바르지 않습니다.', 'INVALID_CREDENTIALS', Response::HTTP_UNAUTHORIZED);
        }

        if (! $user->email_verified_at) {
            throw new BusinessException('이메일 인증이 완료되지 않았습니다.', 'EMAIL_NOT_VERIFIED', Response::HTTP_FORBIDDEN);
        }

        $user->forceFill(['last_login_at' => now()])->save();

        return $this->issueTokenPair($user);
    }

    public function logout(User $user): array
    {
        RefreshToken::query()
            ->where('user_id', $user->id)
            ->whereNull('revoked_at')
            ->update(['revoked_at' => now()]);

        return ['message' => '로그아웃되었습니다.'];
    }

    public function refresh(string $refreshToken): array
    {
        $record = RefreshToken::query()
            ->whereNull('revoked_at')
            ->where('expires_at', '>', now())
            ->get()
            ->first(fn (RefreshToken $item) => Hash::check($refreshToken, $item->token_hash));

        if (! $record) {
            throw new BusinessException('유효하지 않은 리프레시 토큰입니다.', 'INVALID_REFRESH_TOKEN', Response::HTTP_UNAUTHORIZED);
        }

        $user = User::query()->find($record->user_id);

        if (! $user) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_UNAUTHORIZED);
        }

        $record->update(['revoked_at' => now()]);

        return $this->issueTokenPair($user);
    }

    public function requestPasswordReset(string $email, string $phone): array
    {
        $user = User::query()
            ->where('email', $email)
            ->where('phone', $phone)
            ->first();

        if (! $user) {
            throw new BusinessException('이메일과 전화번호가 일치하는 사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $code = $this->issueAuthCode($user, AuthCodePurpose::PASSWORD_RESET);
        $this->sendPasswordResetMail($email, $code);

        return ['message' => '비밀번호 재설정 인증코드가 발송되었습니다.'];
    }

    public function verifyPasswordResetCode(string $email, string $code): array
    {
        $record = $this->findValidCode($email, AuthCodePurpose::PASSWORD_RESET, $code);
        $record->update(['verified_at' => now()]);

        return [
            'resetToken' => $this->createResetToken($email),
        ];
    }

    public function confirmPasswordReset(string $resetToken, string $newPassword): array
    {
        $decoded = base64_decode($resetToken, true);
        [$email] = explode('|', $decoded ?: '|');

        if (! filled($email)) {
            throw new BusinessException('유효하지 않은 재설정 토큰입니다.', 'INVALID_RESET_TOKEN', Response::HTTP_BAD_REQUEST);
        }

        $record = PasswordResetToken::query()->find($email);

        if (! $record || ! Hash::check($resetToken, $record->token)) {
            throw new BusinessException('유효하지 않은 재설정 토큰입니다.', 'INVALID_RESET_TOKEN', Response::HTTP_BAD_REQUEST);
        }

        if ($record->created_at->addSeconds(config('pbshop.auth.password_reset_token_ttl', 1800))->isPast()) {
            throw new BusinessException('만료된 재설정 토큰입니다.', 'EXPIRED_RESET_TOKEN', Response::HTTP_BAD_REQUEST);
        }

        $user = User::query()->where('email', $email)->firstOrFail();
        $user->forceFill([
            'password' => Hash::make($newPassword),
        ])->save();

        PasswordResetToken::query()->where('email', $email)->delete();

        return ['message' => '비밀번호가 재설정되었습니다.'];
    }

    public function socialRedirect(string $provider): string
    {
        $this->ensureSupportedProvider($provider);

        return config('pbshop.api.frontend_url').'/public/login?provider='.$provider.'&mode=mock-social';
    }

    public function socialCallback(string $provider, string $code, ?string $email = null, ?string $name = null): array
    {
        $this->ensureSupportedProvider($provider);

        if (! filled($code)) {
            throw new BusinessException('소셜 인증 code가 필요합니다.', 'SOCIAL_CODE_REQUIRED', Response::HTTP_BAD_REQUEST);
        }

        $socialAccount = SocialAccount::query()
            ->where('provider', $provider)
            ->where('provider_user_id', $code)
            ->first();

        if ($socialAccount) {
            $user = User::query()->findOrFail($socialAccount->user_id);

            return array_merge($this->issueTokenPair($user), ['isNewUser' => false]);
        }

        $providerEmail = $email ?: sprintf('%s_%s@pbshop.social', $provider, $code);
        $displayName = $name ?: ucfirst($provider).' User';
        $user = User::query()->where('email', $providerEmail)->first();
        $isNewUser = false;

        if (! $user) {
            $user = User::query()->create([
                'email' => $providerEmail,
                'password' => Hash::make(Str::random(32)),
                'name' => $displayName,
                'role' => 'USER',
                'status' => 'ACTIVE',
                'email_verified_at' => now(),
            ]);
            $isNewUser = true;
        }

        SocialAccount::query()->create([
            'user_id' => $user->id,
            'provider' => $provider,
            'provider_user_id' => $code,
            'provider_email' => $providerEmail,
        ]);

        return array_merge($this->issueTokenPair($user), ['isNewUser' => $isNewUser]);
    }

    public function completeSocialSignup(User $user, string $phone, string $nickname): array
    {
        $user->forceFill([
            'phone' => $phone,
            'nickname' => $nickname,
        ])->save();

        return $this->issueTokenPair($user);
    }

    public function linkSocial(User $user, string $provider, string $socialToken): array
    {
        $this->ensureSupportedProvider($provider);

        if (SocialAccount::query()->where('provider', $provider)->where('provider_user_id', $socialToken)->exists()) {
            throw new BusinessException('이미 연동된 소셜 계정입니다.', 'SOCIAL_ACCOUNT_ALREADY_LINKED', Response::HTTP_BAD_REQUEST);
        }

        SocialAccount::query()->create([
            'user_id' => $user->id,
            'provider' => $provider,
            'provider_user_id' => $socialToken,
            'provider_email' => $user->email,
        ]);

        return [
            'message' => '소셜 계정이 연동되었습니다.',
            'linkedProvider' => $provider,
        ];
    }

    public function unlinkSocial(User $user, string $provider): array
    {
        $this->ensureSupportedProvider($provider);

        $account = SocialAccount::query()
            ->where('user_id', $user->id)
            ->where('provider', $provider)
            ->first();

        if (! $account) {
            throw new BusinessException('연동된 소셜 계정을 찾을 수 없습니다.', 'SOCIAL_ACCOUNT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $account->delete();

        return ['message' => '소셜 계정 연동이 해제되었습니다.'];
    }

    private function issueAuthCode(User $user, string $purpose): string
    {
        $code = (string) random_int(100000, 999999);

        AuthCode::query()->create([
            'user_id' => $user->id,
            'email' => $user->email,
            'purpose' => $purpose,
            'code' => Hash::make($code),
            'expires_at' => CarbonImmutable::now()->addSeconds(
                $purpose === AuthCodePurpose::EMAIL_VERIFICATION
                    ? config('pbshop.auth.verification_code_ttl', 600)
                    : config('pbshop.auth.password_reset_code_ttl', 600)
            ),
        ]);

        return $code;
    }

    private function findValidCode(string $email, string $purpose, string $code): AuthCode
    {
        $record = AuthCode::query()
            ->where('email', $email)
            ->where('purpose', $purpose)
            ->whereNull('verified_at')
            ->where('expires_at', '>', now())
            ->latest('id')
            ->first();

        if (! $record || ! Hash::check($code, $record->code)) {
            throw new BusinessException('유효하지 않거나 만료된 인증코드입니다.', 'INVALID_AUTH_CODE', Response::HTTP_BAD_REQUEST);
        }

        return $record;
    }

    private function issueTokenPair(User $user): array
    {
        $accessToken = $this->jwtService->createAccessToken($user);
        $refreshToken = Str::random(80);

        RefreshToken::query()->create([
            'user_id' => $user->id,
            'token_hash' => Hash::make($refreshToken),
            'expires_at' => now()->addSeconds(config('pbshop.auth.refresh_ttl', 1209600)),
        ]);

        return [
            'accessToken' => $accessToken,
            'refreshToken' => $refreshToken,
            'expiresIn' => config('pbshop.auth.access_ttl', 3600),
        ];
    }

    private function createResetToken(string $email): string
    {
        $rawToken = base64_encode($email.'|'.Str::random(40));

        PasswordResetToken::query()->updateOrCreate(
            ['email' => $email],
            ['token' => Hash::make($rawToken), 'created_at' => now()]
        );

        return $rawToken;
    }

    private function ensureSupportedProvider(string $provider): void
    {
        if (! in_array($provider, config('pbshop.auth.social_providers', []), true)) {
            throw new BusinessException('지원하지 않는 소셜 로그인 공급자입니다.', 'UNSUPPORTED_SOCIAL_PROVIDER', Response::HTTP_BAD_REQUEST);
        }
    }

    private function sendEmailVerificationMail(string $email, string $code): void
    {
        Mail::raw("PBShop 이메일 인증코드: {$code}", static function ($message) use ($email): void {
            $message->to($email)->subject('PBShop 이메일 인증');
        });
    }

    private function sendPasswordResetMail(string $email, string $code): void
    {
        Mail::raw("PBShop 비밀번호 재설정 인증코드: {$code}", static function ($message) use ($email): void {
            $message->to($email)->subject('PBShop 비밀번호 재설정');
        });
    }
}
