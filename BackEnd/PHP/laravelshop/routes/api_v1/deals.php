<?php

use App\Http\Controllers\Api\V1\Deal\DealController;
use Illuminate\Support\Facades\Route;

Route::get('/deals', [DealController::class, 'index']);

Route::middleware('auth.api')->group(function (): void {
    Route::post('/deals/admin', [DealController::class, 'store']);
    Route::patch('/deals/admin/{id}', [DealController::class, 'update'])->whereNumber('id');
    Route::delete('/deals/admin/{id}', [DealController::class, 'destroy'])->whereNumber('id');
});
