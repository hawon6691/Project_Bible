<?php

namespace App\Http\Controllers\Api\V1\UsedMarket;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\UsedMarket\Services\UsedMarketService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'UsedMarket')]
class UsedMarketController extends ApiController
{
    public function __construct(private readonly UsedMarketService $service) {}

    public function productPrice(int $id)
    {
        return $this->success($this->service->productPrice($id));
    }

    public function categoryPrices(Request $request, int $id)
    {
        return $this->success($this->service->categoryPrices($id, (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function estimateBuild(Request $request, int $buildId)
    {
        return $this->success($this->service->estimateBuild($request->user(), $buildId));
    }
}
