<?php

namespace App\Http\Controllers\Api\V1\Deal;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Deal\Requests\StoreDealRequest;
use App\Modules\Deal\Requests\UpdateDealRequest;
use App\Modules\Deal\Services\DealService;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Deal')]
class DealController extends ApiController
{
    public function __construct(
        private readonly DealService $dealService,
    ) {}

    public function index()
    {
        return $this->success($this->dealService->list(request()->query('type')));
    }

    public function store(StoreDealRequest $request)
    {
        return $this->success($this->dealService->create($request->user(), $request->validated()), status: 201);
    }

    public function update(UpdateDealRequest $request, int $id)
    {
        return $this->success($this->dealService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(int $id)
    {
        return $this->success($this->dealService->remove(request()->user(), $id));
    }
}
