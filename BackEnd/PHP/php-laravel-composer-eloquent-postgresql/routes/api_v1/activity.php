<?php

use App\Http\Controllers\Api\V1\Activity\ActivityController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->prefix('activities')->group(function (): void {
    Route::get('/', [ActivityController::class, 'summary']);
    Route::get('/recent-products', [ActivityController::class, 'recentProducts']);
    Route::post('/recent-products/{productId}', [ActivityController::class, 'addRecentProduct'])->whereNumber('productId');
    Route::get('/searches', [ActivityController::class, 'searches']);
    Route::post('/searches', [ActivityController::class, 'storeSearch']);
    Route::delete('/searches/{id}', [ActivityController::class, 'destroySearch'])->whereNumber('id');
    Route::delete('/searches', [ActivityController::class, 'clearSearches']);
});
