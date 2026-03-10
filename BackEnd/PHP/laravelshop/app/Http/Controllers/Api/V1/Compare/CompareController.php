<?php

namespace App\Http\Controllers\Api\V1\Compare;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Compare\Requests\AddCompareItemRequest;
use App\Modules\Compare\Services\CompareService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Compare')]
class CompareController extends ApiController
{
    public function __construct(private readonly CompareService $service) {}

    public function add(AddCompareItemRequest $request)
    {
        return $this->success($this->service->add((string) $request->header('X-Compare-Key', 'guest'), (int) $request->validated()['productId']));
    }

    public function remove(Request $request, int $productId)
    {
        return $this->success($this->service->remove((string) $request->header('X-Compare-Key', 'guest'), $productId));
    }

    public function index(Request $request)
    {
        return $this->success($this->service->list((string) $request->header('X-Compare-Key', 'guest')));
    }

    public function detail(Request $request)
    {
        return $this->success($this->service->detail((string) $request->header('X-Compare-Key', 'guest')));
    }
}
