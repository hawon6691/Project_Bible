<?php

namespace App\Http\Controllers\Api\V1\Product;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Product\Requests\ListProductsRequest;
use App\Modules\Product\Requests\StoreProductRequest;
use App\Modules\Product\Requests\UpdateProductRequest;
use App\Modules\Product\Services\ProductService;
use Illuminate\Http\Request;

class ProductController extends ApiController
{
    public function __construct(
        private readonly ProductService $productService,
    ) {
    }

    public function index(ListProductsRequest $request)
    {
        return $this->success(
            $this->productService->list($request->validated()),
            status: 200
        );
    }

    public function show(int $id)
    {
        return $this->success($this->productService->detail($id));
    }

    public function store(StoreProductRequest $request)
    {
        return $this->success(
            $this->productService->store($request->user(), $request->validated()),
            status: 201
        );
    }

    public function update(UpdateProductRequest $request, int $id)
    {
        return $this->success($this->productService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->productService->delete($request->user(), $id));
    }
}
