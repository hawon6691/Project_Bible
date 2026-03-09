<?php

use App\Http\Controllers\Api\V1\Analytics\AnalyticsController;
use Illuminate\Support\Facades\Route;

Route::prefix('analytics')->group(function (): void {
    Route::get('/products/{id}/lowest-ever', [AnalyticsController::class, 'lowestEver'])->whereNumber('id');
    Route::get('/products/{id}/unit-price', [AnalyticsController::class, 'unitPrice'])->whereNumber('id');
});
