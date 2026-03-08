<?php

use App\Http\Controllers\Api\V1\Category\CategoryController;
use Illuminate\Support\Facades\Route;

Route::prefix('categories')->group(function (): void {
    Route::get('/', [CategoryController::class, 'index']);
    Route::get('/{id}', [CategoryController::class, 'show'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/', [CategoryController::class, 'store']);
        Route::patch('/{id}', [CategoryController::class, 'update'])->whereNumber('id');
        Route::delete('/{id}', [CategoryController::class, 'destroy'])->whereNumber('id');
    });
});
