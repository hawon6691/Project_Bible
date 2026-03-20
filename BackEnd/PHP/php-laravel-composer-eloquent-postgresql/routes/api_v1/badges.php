<?php

use App\Http\Controllers\Api\V1\Badge\BadgeController;
use Illuminate\Support\Facades\Route;

Route::get('/badges', [BadgeController::class, 'index']);
Route::get('/users/{id}/badges', [BadgeController::class, 'userBadges'])->whereNumber('id');

Route::middleware('auth.api')->group(function (): void {
    Route::get('/badges/me', [BadgeController::class, 'me']);
    Route::post('/admin/badges', [BadgeController::class, 'store']);
    Route::patch('/admin/badges/{id}', [BadgeController::class, 'update'])->whereNumber('id');
    Route::delete('/admin/badges/{id}', [BadgeController::class, 'destroy'])->whereNumber('id');
    Route::post('/admin/badges/{id}/grant', [BadgeController::class, 'grant'])->whereNumber('id');
    Route::delete('/admin/badges/{id}/revoke/{userId}', [BadgeController::class, 'revoke'])->whereNumber(['id', 'userId']);
});
