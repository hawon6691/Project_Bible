<?php

use App\Http\Controllers\Api\V1\Review\ReviewController;
use Illuminate\Support\Facades\Route;

Route::prefix('products')->group(function (): void {
    Route::get('/{productId}/reviews', [ReviewController::class, 'index'])->whereNumber('productId');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/{productId}/reviews', [ReviewController::class, 'store'])->whereNumber('productId');
    });
});

Route::middleware('auth.api')->group(function (): void {
    Route::patch('/reviews/{id}', [ReviewController::class, 'update'])->whereNumber('id');
    Route::delete('/reviews/{id}', [ReviewController::class, 'destroy'])->whereNumber('id');
});
