<?php

use App\Http\Controllers\Api\V1\Trust\TrustController;
use Illuminate\Support\Facades\Route;

Route::get('/trust/sellers/{sellerId}', [TrustController::class, 'seller'])->whereNumber('sellerId');
Route::get('/trust/sellers/{sellerId}/history', [TrustController::class, 'history'])->whereNumber('sellerId');

Route::middleware('auth.api')->group(function (): void {
    Route::post('/trust/admin/sellers/{sellerId}/recalculate', [TrustController::class, 'recalculate'])->whereNumber('sellerId');
});
