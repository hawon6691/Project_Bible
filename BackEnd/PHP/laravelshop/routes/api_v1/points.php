<?php

use App\Http\Controllers\Api\V1\Point\PointController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->group(function (): void {
    Route::get('/points/balance', [PointController::class, 'balance']);
    Route::get('/points/transactions', [PointController::class, 'transactions']);
    Route::post('/admin/points/grant', [PointController::class, 'adminGrant']);
});
