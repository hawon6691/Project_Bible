<?php

use App\Http\Controllers\Api\V1\ErrorCode\ErrorCodeController;
use Illuminate\Support\Facades\Route;

Route::prefix('errors/codes')->group(function (): void {
    Route::get('/', [ErrorCodeController::class, 'index']);
    Route::get('/{key}', [ErrorCodeController::class, 'show']);
});
