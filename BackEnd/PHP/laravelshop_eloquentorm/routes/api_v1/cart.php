<?php

use App\Http\Controllers\Api\V1\Cart\CartController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->prefix('cart')->group(function (): void {
    Route::get('/', [CartController::class, 'index']);
    Route::post('/', [CartController::class, 'store']);
    Route::patch('/{itemId}', [CartController::class, 'update'])->whereNumber('itemId');
    Route::delete('/{itemId}', [CartController::class, 'destroy'])->whereNumber('itemId');
    Route::delete('/', [CartController::class, 'clear']);
});
