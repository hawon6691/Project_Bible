<?php

use App\Http\Controllers\Api\V1\Ranking\RankingController;
use Illuminate\Support\Facades\Route;

Route::get('/rankings/products/popular', [RankingController::class, 'products']);
Route::get('/rankings/keywords/popular', [RankingController::class, 'keywords']);

Route::middleware('auth.api')->group(function (): void {
    Route::post('/rankings/admin/recalculate', [RankingController::class, 'recalculate']);
});
