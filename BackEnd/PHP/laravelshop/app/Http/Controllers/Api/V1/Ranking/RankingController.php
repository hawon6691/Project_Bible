<?php

namespace App\Http\Controllers\Api\V1\Ranking;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Ranking\Services\RankingService;

class RankingController extends ApiController
{
    public function __construct(
        private readonly RankingService $rankingService,
    ) {
    }

    public function products()
    {
        return $this->success($this->rankingService->popularProducts(
            request()->query('categoryId') ? (int) request()->query('categoryId') : null,
            (int) request()->query('limit', 20),
        ));
    }

    public function keywords()
    {
        return $this->success($this->rankingService->popularKeywords((int) request()->query('limit', 20)));
    }

    public function recalculate()
    {
        return $this->success($this->rankingService->recalculate(request()->user()));
    }
}
