<?php

use App\Http\Controllers\Api\V1\Observability\ObservabilityController;
use Illuminate\Support\Facades\Route;

Route::prefix('admin/observability')->middleware('auth.api')->group(function (): void {
    Route::get('/metrics', [ObservabilityController::class, 'metrics']);
    Route::get('/traces', [ObservabilityController::class, 'traces']);
    Route::get('/dashboard', [ObservabilityController::class, 'dashboard']);
});
