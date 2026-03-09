<?php

namespace App\Http\Controllers\Api\V1\Seller;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Seller\Requests\ListSellersRequest;
use App\Modules\Seller\Requests\StoreSellerRequest;
use App\Modules\Seller\Requests\UpdateSellerRequest;
use App\Modules\Seller\Services\SellerService;

class SellerController extends ApiController
{
    public function __construct(
        private readonly SellerService $sellerService,
    ) {}

    public function index(ListSellersRequest $request)
    {
        return $this->success($this->sellerService->list($request->validated()));
    }

    public function show(int $id)
    {
        return $this->success($this->sellerService->detail($id));
    }

    public function store(StoreSellerRequest $request)
    {
        return $this->success($this->sellerService->create($request->user(), $request->validated()), status: 201);
    }

    public function update(UpdateSellerRequest $request, int $id)
    {
        return $this->success($this->sellerService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(int $id)
    {
        return $this->success($this->sellerService->delete(request()->user(), $id));
    }
}
