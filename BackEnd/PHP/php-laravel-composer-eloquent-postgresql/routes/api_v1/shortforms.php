<?php

use App\Http\Controllers\Api\V1\Shortform\ShortformController;
use Illuminate\Support\Facades\Route;

Route::prefix('shortforms')->group(function (): void {
    Route::get('/', [ShortformController::class, 'index']);
    Route::get('/ranking/list', [ShortformController::class, 'ranking']);
    Route::get('/user/{userId}', [ShortformController::class, 'userShortforms'])->whereNumber('userId');
    Route::get('/{id}', [ShortformController::class, 'show'])->whereNumber('id');
    Route::get('/{id}/comments', [ShortformController::class, 'comments'])->whereNumber('id');
    Route::get('/{id}/transcode-status', [ShortformController::class, 'transcodeStatus'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/', [ShortformController::class, 'store']);
        Route::post('/{id}/like', [ShortformController::class, 'like'])->whereNumber('id');
        Route::post('/{id}/comments', [ShortformController::class, 'storeComment'])->whereNumber('id');
        Route::post('/{id}/transcode/retry', [ShortformController::class, 'retry'])->whereNumber('id');
        Route::delete('/{id}', [ShortformController::class, 'destroy'])->whereNumber('id');
    });
});
