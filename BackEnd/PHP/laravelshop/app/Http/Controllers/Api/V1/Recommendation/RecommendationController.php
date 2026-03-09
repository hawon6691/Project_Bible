<?php

namespace App\Http\Controllers\Api\V1\Recommendation;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Recommendation\Requests\StoreRecommendationRequest;
use App\Modules\Recommendation\Services\RecommendationService;

class RecommendationController extends ApiController
{
    public function __construct(
        private readonly RecommendationService $recommendationService,
    ) {}

    public function personal()
    {
        return $this->success($this->recommendationService->personal(request()->user(), (int) request()->query('limit', 10)));
    }

    public function trending()
    {
        return $this->success($this->recommendationService->trending((int) request()->query('limit', 10)));
    }

    public function adminIndex()
    {
        return $this->success($this->recommendationService->adminList(request()->user()));
    }

    public function store(StoreRecommendationRequest $request)
    {
        return $this->success($this->recommendationService->create($request->user(), [
            ...$request->validated(),
            'isActive' => array_key_exists('isActive', $request->all()) ? (bool) $request->input('isActive') : true,
        ]), status: 201);
    }

    public function destroy(int $id)
    {
        return $this->success($this->recommendationService->remove(request()->user(), $id));
    }
}
