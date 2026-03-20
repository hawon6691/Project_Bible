<?php

use App\Http\Controllers\Api\V1\Spec\SpecController;
use Illuminate\Support\Facades\Route;

Route::prefix('specs')->group(function (): void {
    Route::get('/definitions', [SpecController::class, 'listDefinitions']);

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/definitions', [SpecController::class, 'storeDefinition']);
        Route::patch('/definitions/{id}', [SpecController::class, 'updateDefinition'])->whereNumber('id');
        Route::delete('/definitions/{id}', [SpecController::class, 'deleteDefinition'])->whereNumber('id');
    });
});

Route::prefix('products')->group(function (): void {
    Route::get('/{id}/specs', [SpecController::class, 'getProductSpecs'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::put('/{id}/specs', [SpecController::class, 'setProductSpecs'])->whereNumber('id');
    });
});
