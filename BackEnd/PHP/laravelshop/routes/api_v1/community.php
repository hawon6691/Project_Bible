<?php

use App\Http\Controllers\Api\V1\Community\CommunityController;
use Illuminate\Support\Facades\Route;

Route::get('/boards', [CommunityController::class, 'boards']);
Route::get('/boards/{boardId}/posts', [CommunityController::class, 'posts'])->whereNumber('boardId');
Route::get('/posts/{id}', [CommunityController::class, 'showPost'])->whereNumber('id');
Route::get('/posts/{id}/comments', [CommunityController::class, 'comments'])->whereNumber('id');

Route::middleware('auth.api')->group(function (): void {
    Route::post('/boards/{boardId}/posts', [CommunityController::class, 'storePost'])->whereNumber('boardId');
    Route::patch('/posts/{id}', [CommunityController::class, 'updatePost'])->whereNumber('id');
    Route::delete('/posts/{id}', [CommunityController::class, 'destroyPost'])->whereNumber('id');
    Route::post('/posts/{id}/like', [CommunityController::class, 'toggleLike'])->whereNumber('id');
    Route::post('/posts/{id}/comments', [CommunityController::class, 'storeComment'])->whereNumber('id');
    Route::delete('/comments/{id}', [CommunityController::class, 'destroyComment'])->whereNumber('id');
});
