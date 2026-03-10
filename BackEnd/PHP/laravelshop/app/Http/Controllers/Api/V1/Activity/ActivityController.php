<?php

namespace App\Http\Controllers\Api\V1\Activity;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Activity\Requests\CreateSearchHistoryRequest;
use App\Modules\Activity\Services\ActivityService;

#[OA\Tag(name: 'Activity')]
class ActivityController extends ApiController
{
    public function __construct(
        private readonly ActivityService $activityService,
    ) {}

    public function summary()
    {
        return $this->success($this->activityService->getSummary(request()->user()));
    }

    public function recentProducts()
    {
        return $this->success($this->activityService->getRecentProducts(
            request()->user(),
            (int) request()->query('page', 1),
            (int) request()->query('limit', 20),
        ));
    }

    public function addRecentProduct(int $productId)
    {
        return $this->success($this->activityService->addRecentProduct(request()->user(), $productId), status: 201);
    }

    public function searches()
    {
        return $this->success($this->activityService->getSearchHistory(
            request()->user(),
            (int) request()->query('page', 1),
            (int) request()->query('limit', 20),
        ));
    }

    public function storeSearch(CreateSearchHistoryRequest $request)
    {
        return $this->success($this->activityService->addSearchHistory($request->user(), $request->validated()), status: 201);
    }

    public function destroySearch(int $id)
    {
        return $this->success($this->activityService->removeSearchHistory(request()->user(), $id));
    }

    public function clearSearches()
    {
        return $this->success($this->activityService->clearSearchHistory(request()->user()));
    }
}
