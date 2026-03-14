<?php

use App\Http\Controllers\Api\V1\Payment\PaymentController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->prefix('payments')->group(function (): void {
    Route::post('/', [PaymentController::class, 'store']);
    Route::get('/{id}', [PaymentController::class, 'show'])->whereNumber('id');
    Route::post('/{id}/refund', [PaymentController::class, 'refund'])->whereNumber('id');
});
