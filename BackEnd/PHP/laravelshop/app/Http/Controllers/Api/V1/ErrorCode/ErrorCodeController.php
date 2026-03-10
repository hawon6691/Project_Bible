<?php

namespace App\Http\Controllers\Api\V1\ErrorCode;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\ErrorCode\Services\ErrorCodeService;

#[OA\Tag(name: 'ErrorCode')]
class ErrorCodeController extends ApiController
{
    public function __construct(private readonly ErrorCodeService $service) {}

    public function index()
    {
        return $this->success($this->service->list());
    }

    public function show(string $key)
    {
        return $this->success($this->service->show($key));
    }
}
