<?php

namespace App\Http\Controllers\Api\V1\System;

use App\Http\Controllers\Api\V1\ApiController;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'System')]
class SystemController extends ApiController
{
    #[OA\Get(
        path: '/api/v1/health',
        operationId: 'systemHealth',
        summary: '서비스 상태 확인',
        tags: ['System'],
        responses: [
            new OA\Response(
                response: 200,
                description: '정상 응답',
                content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')
            ),
            new OA\Response(response: 500, description: '서버 오류', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    public function health()
    {
        return $this->success([
            'service' => config('app.name'),
            'language' => 'php',
            'framework' => 'laravel',
            'status' => 'ok',
        ]);
    }
}
