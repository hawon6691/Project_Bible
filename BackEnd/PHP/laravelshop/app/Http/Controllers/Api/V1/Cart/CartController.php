<?php

namespace App\Http\Controllers\Api\V1\Cart;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Cart\Requests\StoreCartItemRequest;
use App\Modules\Cart\Requests\UpdateCartItemRequest;
use App\Modules\Cart\Services\CartService;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Cart')]
class CartController extends ApiController
{
    public function __construct(
        private readonly CartService $cartService,
    ) {}

    public function index()
    {
        return $this->success($this->cartService->list(request()->user()));
    }

    public function store(StoreCartItemRequest $request)
    {
        return $this->success($this->cartService->add($request->user(), $request->validated()), status: 201);
    }

    public function update(UpdateCartItemRequest $request, int $itemId)
    {
        return $this->success($this->cartService->update($request->user(), $itemId, $request->validated()['quantity']));
    }

    public function destroy(int $itemId)
    {
        return $this->success($this->cartService->delete(request()->user(), $itemId));
    }

    public function clear()
    {
        return $this->success($this->cartService->clear(request()->user()));
    }
}
