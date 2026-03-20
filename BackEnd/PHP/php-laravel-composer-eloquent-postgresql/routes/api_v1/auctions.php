<?php

use App\Http\Controllers\Api\V1\Auction\AuctionController;
use Illuminate\Support\Facades\Route;

Route::prefix('auctions')->group(function (): void {
    Route::get('/', [AuctionController::class, 'index']);
    Route::get('/{id}', [AuctionController::class, 'show'])->whereNumber('id');

    Route::middleware('auth.api')->group(function (): void {
        Route::post('/', [AuctionController::class, 'store']);
        Route::post('/{id}/bids', [AuctionController::class, 'storeBid'])->whereNumber('id');
        Route::patch('/{id}/bids/{bidId}/select', [AuctionController::class, 'selectBid'])->whereNumber('id')->whereNumber('bidId');
        Route::delete('/{id}', [AuctionController::class, 'destroy'])->whereNumber('id');
        Route::patch('/{id}/bids/{bidId}', [AuctionController::class, 'updateBid'])->whereNumber('id')->whereNumber('bidId');
        Route::delete('/{id}/bids/{bidId}', [AuctionController::class, 'destroyBid'])->whereNumber('id')->whereNumber('bidId');
    });
});
