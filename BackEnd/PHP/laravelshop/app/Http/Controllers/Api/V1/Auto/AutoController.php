<?php

namespace App\Http\Controllers\Api\V1\Auto;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Auto\Requests\EstimateAutoRequest;
use App\Modules\Auto\Services\AutoService;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Auto')]
class AutoController extends ApiController
{
    public function __construct(private readonly AutoService $service) {}

    public function models(Request $request)
    {
        return $this->success($this->service->models($request->query('brand'), $request->query('type')));
    }

    public function trims(int $id)
    {
        return $this->success($this->service->trims($id));
    }

    public function estimate(EstimateAutoRequest $request)
    {
        return $this->success($this->service->estimate($request->validated()));
    }

    public function leaseOffers(int $id)
    {
        return $this->success($this->service->leaseOffers($id));
    }
}
