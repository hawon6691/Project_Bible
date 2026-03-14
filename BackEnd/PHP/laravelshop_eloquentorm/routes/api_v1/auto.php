<?php

use App\Http\Controllers\Api\V1\Auto\AutoController;
use Illuminate\Support\Facades\Route;

Route::prefix('auto')->group(function (): void {
    Route::get('/models', [AutoController::class, 'models']);
    Route::get('/models/{id}/trims', [AutoController::class, 'trims'])->whereNumber('id');
    Route::post('/estimate', [AutoController::class, 'estimate']);
    Route::get('/models/{id}/lease-offers', [AutoController::class, 'leaseOffers'])->whereNumber('id');
});
