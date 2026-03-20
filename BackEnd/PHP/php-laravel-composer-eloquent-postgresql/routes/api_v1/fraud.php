<?php

use App\Http\Controllers\Api\V1\Fraud\FraudController;
use Illuminate\Support\Facades\Route;

Route::get('/products/{id}/real-price', [FraudController::class, 'realPrice'])->whereNumber('id');
Route::get('/fraud/products/{productId}/effective-prices', [FraudController::class, 'effectivePrices'])->whereNumber('productId');
Route::get('/fraud/products/{productId}/anomalies', [FraudController::class, 'anomalies'])->whereNumber('productId');

Route::middleware('auth.api')->group(function (): void {
    Route::get('/fraud/alerts', [FraudController::class, 'alerts']);
    Route::patch('/fraud/alerts/{id}/approve', [FraudController::class, 'approve'])->whereNumber('id');
    Route::patch('/fraud/alerts/{id}/reject', [FraudController::class, 'reject'])->whereNumber('id');
    Route::post('/fraud/admin/products/{productId}/scan', [FraudController::class, 'scan'])->whereNumber('productId');
    Route::get('/fraud/admin/products/{productId}/flags', [FraudController::class, 'flags'])->whereNumber('productId');
});
