<?php

namespace App\Http\Controllers\Api\V1;

use App\Common\Http\ApiResponse;
use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;

abstract class ApiController extends Controller
{
    protected function success(mixed $data = null, array $meta = [], int $status = 200): JsonResponse
    {
        return ApiResponse::success($data, $meta, $status);
    }

    protected function error(string $message, string $code = 'INTERNAL_SERVER_ERROR', int $status = 500, array $details = []): JsonResponse
    {
        return ApiResponse::error($message, $code, $status, $details);
    }
}
