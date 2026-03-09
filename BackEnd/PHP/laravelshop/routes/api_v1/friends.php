<?php

use App\Http\Controllers\Api\V1\Friend\FriendController;
use Illuminate\Support\Facades\Route;

Route::prefix('friends')->middleware('auth.api')->group(function (): void {
    Route::post('/request/{userId}', [FriendController::class, 'request'])->whereNumber('userId');
    Route::patch('/request/{friendshipId}/accept', [FriendController::class, 'accept'])->whereNumber('friendshipId');
    Route::patch('/request/{friendshipId}/reject', [FriendController::class, 'reject'])->whereNumber('friendshipId');
    Route::get('/', [FriendController::class, 'index']);
    Route::get('/requests/received', [FriendController::class, 'received']);
    Route::get('/requests/sent', [FriendController::class, 'sent']);
    Route::get('/feed', [FriendController::class, 'feed']);
    Route::post('/block/{userId}', [FriendController::class, 'block'])->whereNumber('userId');
    Route::delete('/block/{userId}', [FriendController::class, 'unblock'])->whereNumber('userId');
    Route::delete('/{userId}', [FriendController::class, 'destroy'])->whereNumber('userId');
});
