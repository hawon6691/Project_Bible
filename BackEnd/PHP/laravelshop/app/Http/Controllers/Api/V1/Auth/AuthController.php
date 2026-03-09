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

class AuthController extends ApiController
{
    public function __construct(
        private readonly AuthService $authService,
    ) {}

    public function signup(SignupRequest $request)
    {
        return $this->success($this->authService->signup($request->validated()), status: 201);
    }

    public function verifyEmail(VerifyEmailRequest $request)
    {
        return $this->success($this->authService->verifyEmail(
            $request->string('email')->value(),
            $request->string('code')->value(),
        ));
    }

    public function resendVerification(ResendVerificationRequest $request)
    {
        return $this->success($this->authService->resendVerification($request->string('email')->value()));
    }

    public function login(LoginRequest $request)
    {
        return $this->success($this->authService->login(
            $request->string('email')->value(),
            $request->string('password')->value(),
        ));
    }

    public function logout(Request $request)
    {
        return $this->success($this->authService->logout($request->user()));
    }

    public function refresh(RefreshTokenRequest $request)
    {
        return $this->success($this->authService->refresh($request->string('refreshToken')->value()));
    }

    public function requestPasswordReset(PasswordResetRequestRequest $request)
    {
        return $this->success($this->authService->requestPasswordReset(
            $request->string('email')->value(),
            $request->string('phone')->value(),
        ));
    }

    public function verifyPasswordResetCode(PasswordResetVerifyRequest $request)
    {
        return $this->success($this->authService->verifyPasswordResetCode(
            $request->string('email')->value(),
            $request->string('code')->value(),
        ));
    }

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
