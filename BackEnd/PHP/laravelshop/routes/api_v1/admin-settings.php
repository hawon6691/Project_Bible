<?php

use App\Http\Controllers\Api\V1\AdminSettings\AdminSettingsController;
use Illuminate\Support\Facades\Route;

Route::prefix('admin/settings')->middleware('auth.api')->group(function (): void {
    Route::get('/extensions', [AdminSettingsController::class, 'extensions']);
    Route::post('/extensions', [AdminSettingsController::class, 'updateExtensions']);
    Route::get('/upload-limits', [AdminSettingsController::class, 'uploadLimits']);
    Route::patch('/upload-limits', [AdminSettingsController::class, 'updateUploadLimits']);
    Route::get('/review-policy', [AdminSettingsController::class, 'reviewPolicy']);
    Route::patch('/review-policy', [AdminSettingsController::class, 'updateReviewPolicy']);
});
