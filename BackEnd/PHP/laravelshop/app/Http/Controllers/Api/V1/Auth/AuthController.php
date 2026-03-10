<?php

namespace App\Http\Controllers\Api\V1\Auth;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Auth\Requests\LoginRequest;
use App\Modules\Auth\Requests\PasswordResetConfirmRequest;
use App\Modules\Auth\Requests\PasswordResetRequestRequest;
use App\Modules\Auth\Requests\PasswordResetVerifyRequest;
use App\Modules\Auth\Requests\RefreshTokenRequest;
use App\Modules\Auth\Requests\ResendVerificationRequest;
use App\Modules\Auth\Requests\SignupRequest;
use App\Modules\Auth\Requests\SocialCompleteRequest;
use App\Modules\Auth\Requests\SocialLinkRequest;
use App\Modules\Auth\Requests\VerifyEmailRequest;
use App\Modules\Auth\Services\AuthService;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Auth')]
class AuthController extends ApiController
{
    public function __construct(
        private readonly AuthService $authService,
    ) {}

    #[OA\Post(
        path: '/api/v1/auth/signup',
        operationId: 'authSignup',
        summary: '회원가입',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'password', 'name', 'phone'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'user@example.com'),
                    new OA\Property(property: 'password', type: 'string', format: 'password', example: 'Password123!'),
                    new OA\Property(property: 'name', type: 'string', example: '홍길동'),
                    new OA\Property(property: 'phone', type: 'string', example: '01012345678'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 201, description: '회원가입 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 422, description: '입력값 검증 실패', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ],
    )]
    public function signup(SignupRequest $request)
    {
        return $this->success($this->authService->signup($request->validated()), status: 201);
    }

    #[OA\Post(
        path: '/api/v1/auth/verify-email',
        operationId: 'authVerifyEmail',
        summary: '이메일 인증 코드 확인',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'code'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'user@example.com'),
                    new OA\Property(property: 'code', type: 'string', example: '123456'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '인증 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function verifyEmail(VerifyEmailRequest $request)
    {
        return $this->success($this->authService->verifyEmail(
            $request->string('email')->value(),
            $request->string('code')->value(),
        ));
    }

    #[OA\Post(
        path: '/api/v1/auth/resend-verification',
        operationId: 'authResendVerification',
        summary: '인증 메일 재발송',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'user@example.com'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '재발송 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function resendVerification(ResendVerificationRequest $request)
    {
        return $this->success($this->authService->resendVerification($request->string('email')->value()));
    }

    #[OA\Post(
        path: '/api/v1/auth/login',
        operationId: 'authLogin',
        summary: '로그인',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'password'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'user@example.com'),
                    new OA\Property(property: 'password', type: 'string', format: 'password', example: 'Password123!'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '로그인 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 401, description: '인증 실패', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ],
    )]
    public function login(LoginRequest $request)
    {
        return $this->success($this->authService->login(
            $request->string('email')->value(),
            $request->string('password')->value(),
        ));
    }

    #[OA\Post(
        path: '/api/v1/auth/logout',
        operationId: 'authLogout',
        summary: '로그아웃',
        security: [['bearerAuth' => []]],
        tags: ['Auth'],
        responses: [
            new OA\Response(response: 200, description: '로그아웃 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function logout(Request $request)
    {
        return $this->success($this->authService->logout($request->user()));
    }

    #[OA\Post(
        path: '/api/v1/auth/refresh',
        operationId: 'authRefresh',
        summary: '토큰 갱신',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['refreshToken'],
                properties: [
                    new OA\Property(property: 'refreshToken', type: 'string', example: 'refresh-token'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '토큰 갱신 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function refresh(RefreshTokenRequest $request)
    {
        return $this->success($this->authService->refresh($request->string('refreshToken')->value()));
    }

    #[OA\Post(
        path: '/api/v1/auth/password-reset/request',
        operationId: 'authPasswordResetRequest',
        summary: '비밀번호 재설정 요청',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'phone'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'user@example.com'),
                    new OA\Property(property: 'phone', type: 'string', example: '01012345678'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '요청 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function requestPasswordReset(PasswordResetRequestRequest $request)
    {
        return $this->success($this->authService->requestPasswordReset(
            $request->string('email')->value(),
            $request->string('phone')->value(),
        ));
    }

    #[OA\Post(
        path: '/api/v1/auth/password-reset/verify',
        operationId: 'authPasswordResetVerify',
        summary: '비밀번호 재설정 코드 확인',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'code'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'user@example.com'),
                    new OA\Property(property: 'code', type: 'string', example: '123456'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '코드 확인 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function verifyPasswordResetCode(PasswordResetVerifyRequest $request)
    {
        return $this->success($this->authService->verifyPasswordResetCode(
            $request->string('email')->value(),
            $request->string('code')->value(),
        ));
    }

    #[OA\Post(
        path: '/api/v1/auth/password-reset/confirm',
        operationId: 'authPasswordResetConfirm',
        summary: '비밀번호 재설정 완료',
        tags: ['Auth'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['resetToken', 'newPassword'],
                properties: [
                    new OA\Property(property: 'resetToken', type: 'string', example: 'reset-token'),
                    new OA\Property(property: 'newPassword', type: 'string', format: 'password', example: 'NewPassword123!'),
                ],
            ),
        ),
        responses: [
            new OA\Response(response: 200, description: '재설정 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function confirmPasswordReset(PasswordResetConfirmRequest $request)
    {
        return $this->success($this->authService->confirmPasswordReset(
            $request->string('resetToken')->value(),
            $request->string('newPassword')->value(),
        ));
    }

    public function socialRedirect(string $provider)
    {
        return redirect()->away($this->authService->socialRedirect($provider));
    }

    #[OA\Get(
        path: '/api/v1/auth/callback/{provider}',
        operationId: 'authSocialCallback',
        summary: '소셜 로그인 콜백',
        tags: ['Auth'],
        parameters: [
            new OA\Parameter(name: 'provider', in: 'path', required: true, schema: new OA\Schema(type: 'string', example: 'google')),
            new OA\Parameter(name: 'code', in: 'query', required: false, schema: new OA\Schema(type: 'string')),
            new OA\Parameter(name: 'email', in: 'query', required: false, schema: new OA\Schema(type: 'string', format: 'email')),
            new OA\Parameter(name: 'name', in: 'query', required: false, schema: new OA\Schema(type: 'string')),
        ],
        responses: [
            new OA\Response(response: 200, description: '소셜 로그인 처리 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
        ],
    )]
    public function socialCallback(Request $request, string $provider)
    {
        return $this->success($this->authService->socialCallback(
            $provider,
            (string) $request->query('code', ''),
            $request->query('email'),
            $request->query('name'),
        ));
    }

    public function socialComplete(SocialCompleteRequest $request)
    {
        return $this->success($this->authService->completeSocialSignup(
            $request->user(),
            $request->string('phone')->value(),
            $request->string('nickname')->value(),
        ));
    }

    public function socialLink(SocialLinkRequest $request)
    {
        return $this->success($this->authService->linkSocial(
            $request->user(),
            $request->string('provider')->value(),
            $request->string('socialToken')->value(),
        ));
    }

    public function socialUnlink(Request $request, string $provider)
    {
        return $this->success($this->authService->unlinkSocial($request->user(), $provider));
    }
}
