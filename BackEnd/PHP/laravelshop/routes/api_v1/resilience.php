<?php

use App\Http\Controllers\Api\V1\Resilience\ResilienceController;
use Illuminate\Support\Facades\Route;

Route::prefix('resilience/circuit-breakers')->middleware('auth.api')->group(function (): void {
    Route::get('/', [ResilienceController::class, 'index']);
    Route::get('/policies', [ResilienceController::class, 'policies']);
    Route::get('/{name}', [ResilienceController::class, 'show']);
    Route::post('/{name}/reset', [ResilienceController::class, 'reset']);
});
