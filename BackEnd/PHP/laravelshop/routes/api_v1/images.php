<?php

use App\Http\Controllers\Api\V1\Image\ImageController;
use Illuminate\Support\Facades\Route;

Route::get('/images/{id}/variants', [ImageController::class, 'variants'])->whereNumber('id');

Route::middleware('auth.api')->group(function (): void {
    Route::post('/images/upload', [ImageController::class, 'upload']);
    Route::delete('/images/{id}', [ImageController::class, 'destroy'])->whereNumber('id');
});
