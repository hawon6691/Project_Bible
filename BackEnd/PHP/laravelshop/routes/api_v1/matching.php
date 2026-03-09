<?php

use App\Http\Controllers\Api\V1\Matching\MatchingController;
use Illuminate\Support\Facades\Route;

Route::prefix('matching')->middleware('auth.api')->group(function (): void {
    Route::get('/pending', [MatchingController::class, 'pending']);
    Route::patch('/{id}/approve', [MatchingController::class, 'approve'])->whereNumber('id');
    Route::patch('/{id}/reject', [MatchingController::class, 'reject'])->whereNumber('id');
    Route::post('/auto-match', [MatchingController::class, 'autoMatch']);
    Route::get('/stats', [MatchingController::class, 'stats']);
});
