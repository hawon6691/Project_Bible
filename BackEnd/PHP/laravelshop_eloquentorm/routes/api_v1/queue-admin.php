<?php

use App\Http\Controllers\Api\V1\QueueAdmin\QueueAdminController;
use Illuminate\Support\Facades\Route;

Route::prefix('admin/queues')->middleware('auth.api')->group(function (): void {
    Route::get('/supported', [QueueAdminController::class, 'supported']);
    Route::get('/stats', [QueueAdminController::class, 'stats']);
    Route::post('/auto-retry', [QueueAdminController::class, 'autoRetry']);
    Route::get('/{queueName}/failed', [QueueAdminController::class, 'failed']);
    Route::post('/{queueName}/failed/retry', [QueueAdminController::class, 'retryFailed']);
    Route::post('/{queueName}/jobs/{jobId}/retry', [QueueAdminController::class, 'retryJob']);
    Route::delete('/{queueName}/jobs/{jobId}', [QueueAdminController::class, 'removeJob']);
});
