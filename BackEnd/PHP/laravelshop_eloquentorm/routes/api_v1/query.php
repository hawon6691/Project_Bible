<?php

use App\Http\Controllers\Api\V1\Query\QueryController;
use Illuminate\Support\Facades\Route;

Route::prefix('query/products')->group(function (): void {
    Route::get('/', [QueryController::class, 'index']);
    Route::get('/{productId}', [QueryController::class, 'show'])->whereNumber('productId');
});

Route::prefix('admin/query/products')->middleware('auth.api')->group(function (): void {
    Route::post('/rebuild', [QueryController::class, 'rebuild']);
    Route::post('/{productId}/sync', [QueryController::class, 'sync'])->whereNumber('productId');
});
