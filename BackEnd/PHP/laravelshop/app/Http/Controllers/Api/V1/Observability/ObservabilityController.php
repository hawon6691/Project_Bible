<?php

namespace App\Http\Controllers\Api\V1\Observability;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Observability\Services\ObservabilityService;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Observability')]
class ObservabilityController extends ApiController
{
    public function __construct(private readonly ObservabilityService $service) {}

    public function metrics(Request $request)
    {
        return $this->success($this->service->metrics($request->user()));
    }

    public function traces(Request $request)
    {
        return $this->success($this->service->traces($request->user(), (int) $request->query('limit', 20), $request->query('pathContains')));
    }

    public function dashboard(Request $request)
    {
        return $this->success($this->service->dashboard($request->user()));
    }
}
