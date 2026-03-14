<?php

use App\Http\Controllers\Api\V1\Price\PriceController;
use Illuminate\Support\Facades\Route;

Route::prefix('products')->group(function (): void {
    Route::get('/{id}/prices', [PriceController::class, 'listProductPrices'])->whereNumber('id');
    Route::get('/{id}/price-history', [PriceController::class, 'priceHistory'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/{id}/prices', [PriceController::class, 'createProductPrice'])->whereNumber('id');
    });
});

Route::middleware('auth.api')->group(function (): void {
    Route::patch('/prices/{id}', [PriceController::class, 'updatePrice'])->whereNumber('id');
    Route::delete('/prices/{id}', [PriceController::class, 'deletePrice'])->whereNumber('id');
    Route::get('/price-alerts', [PriceController::class, 'listAlerts']);
    Route::post('/price-alerts', [PriceController::class, 'createAlert']);
    Route::delete('/price-alerts/{id}', [PriceController::class, 'deleteAlert'])->whereNumber('id');
});
