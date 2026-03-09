<?php

use App\Http\Controllers\Api\V1\Recommendation\RecommendationController;
use Illuminate\Support\Facades\Route;

Route::get('/recommendations/trending', [RecommendationController::class, 'trending']);

Route::middleware('auth.api')->group(function (): void {
    Route::get('/recommendations/personal', [RecommendationController::class, 'personal']);
    Route::get('/admin/recommendations', [RecommendationController::class, 'adminIndex']);
    Route::post('/admin/recommendations', [RecommendationController::class, 'store']);
    Route::delete('/admin/recommendations/{id}', [RecommendationController::class, 'destroy'])->whereNumber('id');
});
