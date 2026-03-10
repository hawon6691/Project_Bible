<?php

namespace App\Http\Controllers\Api\V1\Matching;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Matching\Requests\ApproveMappingRequest;
use App\Modules\Matching\Requests\RejectMappingRequest;
use App\Modules\Matching\Services\MatchingService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Matching')]
class MatchingController extends ApiController
{
    public function __construct(private readonly MatchingService $service) {}

    public function pending(Request $request)
    {
        return $this->success($this->service->pending($request->user(), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function approve(ApproveMappingRequest $request, int $id)
    {
        return $this->success($this->service->approve($request->user(), $id, (int) $request->validated()['productId']));
    }

    public function reject(RejectMappingRequest $request, int $id)
    {
        return $this->success($this->service->reject($request->user(), $id, (string) $request->validated()['reason']));
    }

    public function autoMatch(Request $request)
    {
        return $this->success($this->service->autoMatch($request->user()));
    }

    public function stats(Request $request)
    {
        return $this->success($this->service->stats($request->user()));
    }
}
