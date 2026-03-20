<?php

use App\Http\Controllers\Api\V1\Chat\ChatController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->prefix('chat')->group(function (): void {
    Route::get('/rooms', [ChatController::class, 'rooms']);
    Route::post('/rooms', [ChatController::class, 'storeRoom']);
    Route::post('/rooms/{id}/join', [ChatController::class, 'joinRoom'])->whereNumber('id');
    Route::get('/rooms/{id}/messages', [ChatController::class, 'messages'])->whereNumber('id');
    Route::post('/rooms/{id}/messages', [ChatController::class, 'storeMessage'])->whereNumber('id');
});
