<?php

use App\Common\Exceptions\BusinessException;
use App\Common\Http\ApiResponse;
use Illuminate\Auth\AuthenticationException;
use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        api: __DIR__.'/../routes/api_v1.php',
        web: __DIR__.'/../routes/web.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->alias([
            'api.locale' => \App\Http\Middleware\SetApiLocale::class,
            'api.context' => \App\Http\Middleware\SetApiRequestContext::class,
        ]);
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        $exceptions->render(function (BusinessException $exception, Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            return ApiResponse::error(
                $exception->getMessage(),
                $exception->errorCode(),
                $exception->status(),
                $exception->context(),
            );
        });

        $exceptions->render(function (ValidationException $exception, Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            return ApiResponse::error(
                '요청 데이터 검증에 실패했습니다.',
                'VALIDATION_ERROR',
                Response::HTTP_UNPROCESSABLE_ENTITY,
                $exception->errors(),
            );
        });

        $exceptions->render(function (AuthenticationException $exception, Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            return ApiResponse::error(
                '인증이 필요합니다.',
                'UNAUTHORIZED',
                Response::HTTP_UNAUTHORIZED,
            );
        });

        $exceptions->render(function (NotFoundHttpException $exception, Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            return ApiResponse::error(
                '요청한 리소스를 찾을 수 없습니다.',
                'NOT_FOUND',
                Response::HTTP_NOT_FOUND,
            );
        });

        $exceptions->render(function (Throwable $exception, Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            return ApiResponse::error(
                config('app.debug') ? $exception->getMessage() : '서버 내부 오류가 발생했습니다.',
                'INTERNAL_SERVER_ERROR',
                Response::HTTP_INTERNAL_SERVER_ERROR,
            );
        });
    })->create();
