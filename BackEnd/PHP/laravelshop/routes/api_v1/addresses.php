<?php

use App\Http\Controllers\Api\V1\Address\AddressController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->prefix('addresses')->group(function (): void {
    Route::get('/', [AddressController::class, 'index']);
    Route::post('/', [AddressController::class, 'store']);
    Route::patch('/{id}', [AddressController::class, 'update'])->whereNumber('id');
    Route::delete('/{id}', [AddressController::class, 'destroy'])->whereNumber('id');
});
