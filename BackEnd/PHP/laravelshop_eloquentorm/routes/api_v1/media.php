<?php

use App\Http\Controllers\Api\V1\Media\MediaController;
use Illuminate\Support\Facades\Route;

Route::prefix('media')->group(function (): void {
    Route::get('/stream/{id}', [MediaController::class, 'stream'])->whereNumber('id');
    Route::get('/{id}/metadata', [MediaController::class, 'metadata'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/upload', [MediaController::class, 'upload']);
        Route::post('/presigned-url', [MediaController::class, 'presignedUrl']);
        Route::delete('/{id}', [MediaController::class, 'destroy'])->whereNumber('id');
    });
});
