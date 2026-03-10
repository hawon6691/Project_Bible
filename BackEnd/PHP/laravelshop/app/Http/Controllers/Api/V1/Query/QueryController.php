<?php

namespace App\Http\Controllers\Api\V1\Query;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Query\Services\QueryService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Query')]
class QueryController extends ApiController
{
    public function __construct(private readonly QueryService $service) {}

    public function index(Request $request)
    {
        return $this->success($this->service->listProducts((int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function show(int $productId)
    {
        return $this->success($this->service->showProduct($productId));
    }

    public function sync(Request $request, int $productId)
    {
        return $this->success($this->service->sync($request->user(), $productId));
    }

    public function rebuild(Request $request)
    {
        return $this->success($this->service->rebuild($request->user()));
    }
}
