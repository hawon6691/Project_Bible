<?php

namespace App\Http\Controllers\Api\V1\Wishlist;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Wishlist\Requests\ListWishlistRequest;
use App\Modules\Wishlist\Services\WishlistService;

class WishlistController extends ApiController
{
    public function __construct(
        private readonly WishlistService $wishlistService,
    ) {}

    public function index(ListWishlistRequest $request)
    {
        return $this->success($this->wishlistService->list($request->user(), $request->validated()));
    }

    public function toggle(int $productId)
    {
        return $this->success($this->wishlistService->toggle(request()->user(), $productId));
    }

    public function destroy(int $productId)
    {
        return $this->success($this->wishlistService->delete(request()->user(), $productId));
    }
}
