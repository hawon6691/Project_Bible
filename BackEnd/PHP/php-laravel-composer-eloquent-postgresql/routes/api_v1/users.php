<?php

use App\Http\Controllers\Api\V1\User\UserController;
use Illuminate\Support\Facades\Route;

Route::prefix('users')->group(function (): void {
    Route::get('/{id}/profile', [UserController::class, 'profile'])
        ->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::get('/me', [UserController::class, 'me']);
        Route::patch('/me', [UserController::class, 'updateMe']);
        Route::delete('/me', [UserController::class, 'deleteMe']);
        Route::patch('/me/profile', [UserController::class, 'updateProfile']);
        Route::post('/me/profile-image', [UserController::class, 'uploadProfileImage']);
        Route::delete('/me/profile-image', [UserController::class, 'deleteProfileImage']);

        Route::get('/', [UserController::class, 'index']);
        Route::patch('/{id}/status', [UserController::class, 'updateStatus'])
            ->whereNumber('id');
    });
});
