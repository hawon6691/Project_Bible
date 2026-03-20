<?php

use App\Http\Controllers\Api\V1\Compare\CompareController;
use Illuminate\Support\Facades\Route;

Route::prefix('compare')->group(function (): void {
    Route::post('/add', [CompareController::class, 'add']);
    Route::delete('/{productId}', [CompareController::class, 'remove'])->whereNumber('productId');
    Route::get('/', [CompareController::class, 'index']);
    Route::get('/detail', [CompareController::class, 'detail']);
});
