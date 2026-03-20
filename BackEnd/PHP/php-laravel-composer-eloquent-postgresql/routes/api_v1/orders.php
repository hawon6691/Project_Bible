<?php

use App\Http\Controllers\Api\V1\Order\OrderController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->group(function (): void {
    Route::prefix('orders')->group(function (): void {
        Route::post('/', [OrderController::class, 'store']);
        Route::get('/', [OrderController::class, 'index']);
        Route::get('/{id}', [OrderController::class, 'show'])->whereNumber('id');
        Route::post('/{id}/cancel', [OrderController::class, 'cancel'])->whereNumber('id');
    });

    Route::prefix('admin/orders')->group(function (): void {
        Route::get('/', [OrderController::class, 'adminIndex']);
        Route::patch('/{id}/status', [OrderController::class, 'adminUpdateStatus'])->whereNumber('id');
    });
});
