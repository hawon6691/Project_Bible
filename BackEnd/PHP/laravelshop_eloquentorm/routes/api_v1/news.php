<?php

use App\Http\Controllers\Api\V1\News\NewsController;
use Illuminate\Support\Facades\Route;

Route::prefix('news')->group(function (): void {
    Route::get('/', [NewsController::class, 'index']);
    Route::get('/categories', [NewsController::class, 'categories']);
    Route::get('/{id}', [NewsController::class, 'show'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/', [NewsController::class, 'store']);
        Route::patch('/{id}', [NewsController::class, 'update'])->whereNumber('id');
        Route::delete('/{id}', [NewsController::class, 'destroy'])->whereNumber('id');
        Route::post('/categories', [NewsController::class, 'storeCategory']);
        Route::delete('/categories/{id}', [NewsController::class, 'destroyCategory'])->whereNumber('id');
    });
});
