<?php

namespace App\Http\Controllers\Api\V1\Analytics;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Analytics\Services\AnalyticsService;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Analytics')]
class AnalyticsController extends ApiController
{
    public function __construct(private readonly AnalyticsService $service) {}

    public function lowestEver(int $id)
    {
        return $this->success($this->service->lowestEver($id));
    }

    public function unitPrice(int $id)
    {
        return $this->success($this->service->unitPrice($id));
    }
}
