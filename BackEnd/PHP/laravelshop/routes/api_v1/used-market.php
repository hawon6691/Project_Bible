<?php

use App\Http\Controllers\Api\V1\UsedMarket\UsedMarketController;
use Illuminate\Support\Facades\Route;

Route::prefix('used-market')->group(function (): void {
    Route::get('/products/{id}/price', [UsedMarketController::class, 'productPrice'])->whereNumber('id');
    Route::get('/categories/{id}/prices', [UsedMarketController::class, 'categoryPrices'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/pc-builds/{buildId}/estimate', [UsedMarketController::class, 'estimateBuild'])->whereNumber('buildId');
    });
});
