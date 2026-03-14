<?php

use App\Http\Controllers\Api\V1\PcBuilder\PcBuildController;
use Illuminate\Support\Facades\Route;

Route::prefix('pc-builds')->group(function (): void {
    Route::get('/popular', [PcBuildController::class, 'popular']);
    Route::get('/shared/{shareCode}', [PcBuildController::class, 'shared']);
    Route::get('/{id}/compatibility', [PcBuildController::class, 'compatibility'])->whereNumber('id');
    Route::get('/{id}', [PcBuildController::class, 'show'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::get('/', [PcBuildController::class, 'index']);
        Route::post('/', [PcBuildController::class, 'store']);
        Route::patch('/{id}', [PcBuildController::class, 'update'])->whereNumber('id');
        Route::delete('/{id}', [PcBuildController::class, 'destroy'])->whereNumber('id');
        Route::post('/{id}/parts', [PcBuildController::class, 'storePart'])->whereNumber('id');
        Route::delete('/{id}/parts/{partId}', [PcBuildController::class, 'destroyPart'])->whereNumber('id')->whereNumber('partId');
        Route::get('/{id}/share', [PcBuildController::class, 'share'])->whereNumber('id');
    });
});

Route::prefix('admin/compatibility-rules')->middleware('auth.api')->group(function (): void {
    Route::get('/', [PcBuildController::class, 'listRules']);
    Route::post('/', [PcBuildController::class, 'storeRule']);
    Route::patch('/{id}', [PcBuildController::class, 'updateRule'])->whereNumber('id');
    Route::delete('/{id}', [PcBuildController::class, 'destroyRule'])->whereNumber('id');
});
