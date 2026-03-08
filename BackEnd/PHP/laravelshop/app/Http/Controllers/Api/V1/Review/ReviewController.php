<?php

namespace App\Http\Controllers\Api\V1\Review;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Review\Requests\StoreReviewRequest;
use App\Modules\Review\Requests\UpdateReviewRequest;
use App\Modules\Review\Services\ReviewService;

class ReviewController extends ApiController
{
    public function __construct(
        private readonly ReviewService $reviewService,
    ) {
    }

    public function index(int $productId)
    {
        return $this->success($this->reviewService->listByProduct($productId));
    }

    public function store(StoreReviewRequest $request, int $productId)
    {
        return $this->success($this->reviewService->create($request->user(), $productId, $request->validated()), status: 201);
    }

    public function update(UpdateReviewRequest $request, int $id)
    {
        return $this->success($this->reviewService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(int $id)
    {
        return $this->success($this->reviewService->delete(request()->user(), $id));
    }
}
