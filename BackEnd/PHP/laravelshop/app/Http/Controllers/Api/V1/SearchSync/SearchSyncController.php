<?php

namespace App\Http\Controllers\Api\V1\SearchSync;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\SearchSync\Services\SearchSyncService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'SearchSync')]
class SearchSyncController extends ApiController
{
    public function __construct(private readonly SearchSyncService $service) {}

    public function summary(Request $request)
    {
        return $this->success($this->service->summary($request->user()));
    }

    public function requeueFailed(Request $request)
    {
        return $this->success($this->service->requeueFailed($request->user(), (int) $request->query('limit', 10)));
    }
}
