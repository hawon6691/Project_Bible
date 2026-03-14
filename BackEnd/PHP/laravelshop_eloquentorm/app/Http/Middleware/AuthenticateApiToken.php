<?php

namespace App\Http\Middleware;

use App\Common\Exceptions\BusinessException;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Symfony\Component\HttpFoundation\Response;

class AuthenticateApiToken
{
    public function __construct(
        private readonly JwtService $jwtService,
    ) {}

    public function handle(Request $request, Closure $next): Response
    {
        $token = $request->bearerToken();

        if (! $token) {
            throw new BusinessException('인증이 필요합니다.', 'UNAUTHORIZED', Response::HTTP_UNAUTHORIZED);
        }

        try {
            $payload = $this->jwtService->decode($token);
        } catch (\Throwable) {
            throw new BusinessException('유효하지 않은 액세스 토큰입니다.', 'INVALID_ACCESS_TOKEN', Response::HTTP_UNAUTHORIZED);
        }

        if (($payload->type ?? null) !== 'access') {
            throw new BusinessException('유효하지 않은 액세스 토큰입니다.', 'INVALID_ACCESS_TOKEN', Response::HTTP_UNAUTHORIZED);
        }

        $user = User::query()->find($payload->sub ?? 0);

        if (! $user) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_UNAUTHORIZED);
        }

        Auth::setUser($user);
        $request->setUserResolver(static fn () => $user);

        return $next($request);
    }
}
