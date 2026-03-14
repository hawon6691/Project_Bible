<?php

use App\Http\Controllers\Api\V1\Crawler\CrawlerController;
use Illuminate\Support\Facades\Route;

Route::prefix('crawler/admin')->middleware('auth.api')->group(function (): void {
    Route::get('/jobs', [CrawlerController::class, 'jobs']);
    Route::post('/jobs', [CrawlerController::class, 'storeJob']);
    Route::patch('/jobs/{id}', [CrawlerController::class, 'updateJob'])->whereNumber('id');
    Route::delete('/jobs/{id}', [CrawlerController::class, 'destroyJob'])->whereNumber('id');
    Route::post('/jobs/{id}/run', [CrawlerController::class, 'runJob'])->whereNumber('id');
    Route::post('/triggers', [CrawlerController::class, 'trigger']);
    Route::get('/runs', [CrawlerController::class, 'runs']);
    Route::get('/monitoring', [CrawlerController::class, 'monitoring']);
});
