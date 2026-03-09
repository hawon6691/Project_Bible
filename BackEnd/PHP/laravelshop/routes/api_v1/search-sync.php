<?php

use App\Http\Controllers\Api\V1\SearchSync\SearchSyncController;
use Illuminate\Support\Facades\Route;

Route::prefix('search/admin/index/outbox')->middleware('auth.api')->group(function (): void {
    Route::get('/summary', [SearchSyncController::class, 'summary']);
    Route::post('/requeue-failed', [SearchSyncController::class, 'requeueFailed']);
});
