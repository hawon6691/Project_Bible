<?php

namespace App\Http\Controllers\Api\V1\Point;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Point\Requests\AdminGrantPointRequest;
use App\Modules\Point\Requests\ListPointTransactionsRequest;
use App\Modules\Point\Services\PointService;

class PointController extends ApiController
{
    public function __construct(
        private readonly PointService $pointService,
    ) {
    }

    public function balance()
    {
        return $this->success($this->pointService->getBalance(request()->user()));
    }

    public function transactions(ListPointTransactionsRequest $request)
    {
        return $this->success($this->pointService->listTransactions($request->user(), $request->validated()));
    }

    public function adminGrant(AdminGrantPointRequest $request)
    {
        return $this->success($this->pointService->grantByAdmin($request->user(), $request->validated()), status: 201);
    }
}
