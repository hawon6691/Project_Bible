<?php

use App\Http\Controllers\Api\V1\Push\PushController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->prefix('push')->group(function (): void {
    Route::post('/subscriptions', [PushController::class, 'storeSubscription']);
    Route::post('/subscriptions/unsubscribe', [PushController::class, 'unsubscribe']);
    Route::get('/subscriptions', [PushController::class, 'subscriptions']);
    Route::get('/preferences', [PushController::class, 'preference']);
    Route::post('/preferences', [PushController::class, 'updatePreference']);
});
