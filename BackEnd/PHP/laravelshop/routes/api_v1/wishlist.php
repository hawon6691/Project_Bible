<?php

use App\Http\Controllers\Api\V1\Wishlist\WishlistController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->group(function (): void {
    Route::get('/wishlist', [WishlistController::class, 'index']);
    Route::post('/wishlist/{productId}', [WishlistController::class, 'toggle'])->whereNumber('productId');
    Route::delete('/wishlist/{productId}', [WishlistController::class, 'destroy'])->whereNumber('productId');
});
