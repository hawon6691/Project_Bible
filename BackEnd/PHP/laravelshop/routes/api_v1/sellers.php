<?php

use App\Http\Controllers\Api\V1\Seller\SellerController;
use Illuminate\Support\Facades\Route;

Route::prefix('sellers')->group(function (): void {
    Route::get('/', [SellerController::class, 'index']);
    Route::get('/{id}', [SellerController::class, 'show'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/', [SellerController::class, 'store']);
        Route::patch('/{id}', [SellerController::class, 'update'])->whereNumber('id');
        Route::delete('/{id}', [SellerController::class, 'destroy'])->whereNumber('id');
    });
});
