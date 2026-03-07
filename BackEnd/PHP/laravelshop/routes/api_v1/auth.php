<?php

use App\Http\Controllers\Api\V1\Auth\AuthController;
use Illuminate\Support\Facades\Route;

Route::prefix('auth')->group(function (): void {
    Route::post('/signup', [AuthController::class, 'signup']);
    Route::post('/verify-email', [AuthController::class, 'verifyEmail']);
    Route::post('/resend-verification', [AuthController::class, 'resendVerification']);
    Route::post('/login', [AuthController::class, 'login']);
    Route::post('/refresh', [AuthController::class, 'refresh']);
    Route::post('/password-reset/request', [AuthController::class, 'requestPasswordReset']);
    Route::post('/password-reset/verify', [AuthController::class, 'verifyPasswordResetCode']);
    Route::post('/password-reset/confirm', [AuthController::class, 'confirmPasswordReset']);
    Route::get('/login/{provider}', [AuthController::class, 'socialRedirect']);
    Route::get('/callback/{provider}', [AuthController::class, 'socialCallback']);

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/logout', [AuthController::class, 'logout']);
        Route::post('/social/complete', [AuthController::class, 'socialComplete']);
        Route::post('/social/link', [AuthController::class, 'socialLink']);
        Route::delete('/social/unlink/{provider}', [AuthController::class, 'socialUnlink']);
    });
});
