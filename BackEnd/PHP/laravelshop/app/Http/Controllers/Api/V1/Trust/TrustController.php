<?php

namespace App\Http\Controllers\Api\V1\Trust;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Trust\Services\TrustService;

class TrustController extends ApiController
{
    public function __construct(
        private readonly TrustService $trustService,
    ) {}

    public function seller(int $sellerId)
    {
        return $this->success($this->trustService->getCurrentScore($sellerId));
    }

    public function history(int $sellerId)
    {
        return $this->success($this->trustService->getHistory($sellerId, (int) request()->query('limit', 20)));
    }

    public function recalculate(int $sellerId)
    {
        return $this->success($this->trustService->recalculate(request()->user(), $sellerId));
    }
}
