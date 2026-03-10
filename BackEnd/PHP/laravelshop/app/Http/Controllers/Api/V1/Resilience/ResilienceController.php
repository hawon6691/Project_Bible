<?php

namespace App\Http\Controllers\Api\V1\Resilience;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Resilience\Services\ResilienceService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Resilience')]
class ResilienceController extends ApiController
{
    public function __construct(private readonly ResilienceService $service) {}

    public function index(Request $request)
    {
        return $this->success($this->service->list($request->user()));
    }

    public function policies(Request $request)
    {
        return $this->success($this->service->policies($request->user()));
    }

    public function show(Request $request, string $name)
    {
        return $this->success($this->service->show($request->user(), $name));
    }

    public function reset(Request $request, string $name)
    {
        return $this->success($this->service->reset($request->user(), $name));
    }
}
