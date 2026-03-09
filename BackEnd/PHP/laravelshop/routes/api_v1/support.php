<?php

use App\Http\Controllers\Api\V1\Support\SupportController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth.api')->group(function (): void {
    Route::get('/support/tickets', [SupportController::class, 'index']);
    Route::post('/support/tickets', [SupportController::class, 'store']);
    Route::get('/support/tickets/{id}', [SupportController::class, 'show'])->whereNumber('id');
    Route::post('/support/tickets/{id}/reply', [SupportController::class, 'reply'])->whereNumber('id');
    Route::get('/admin/support/tickets', [SupportController::class, 'adminIndex']);
    Route::patch('/admin/support/tickets/{id}/status', [SupportController::class, 'adminUpdateStatus'])->whereNumber('id');
});
