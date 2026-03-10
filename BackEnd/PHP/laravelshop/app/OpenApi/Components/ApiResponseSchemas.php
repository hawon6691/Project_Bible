<?php

namespace App\OpenApi\Components;

use OpenApi\Attributes as OA;

#[OA\Schema(
    schema: 'ApiMeta',
    type: 'object',
    description: '공통 메타 정보',
    properties: [
        new OA\Property(property: 'requestId', type: 'string', nullable: true, example: 'req_123456'),
        new OA\Property(property: 'locale', type: 'string', nullable: true, example: 'ko'),
        new OA\Property(property: 'currency', type: 'string', nullable: true, example: 'KRW'),
        new OA\Property(property: 'page', type: 'integer', nullable: true, example: 1),
        new OA\Property(property: 'limit', type: 'integer', nullable: true, example: 20),
        new OA\Property(property: 'totalCount', type: 'integer', nullable: true, example: 100),
        new OA\Property(property: 'totalPages', type: 'integer', nullable: true, example: 5),
    ],
    additionalProperties: true
)]
#[OA\Schema(
    schema: 'ApiError',
    type: 'object',
    required: ['code', 'message'],
    properties: [
        new OA\Property(property: 'code', type: 'string', example: 'VALIDATION_ERROR'),
        new OA\Property(property: 'message', type: 'string', example: '요청 데이터 검증에 실패했습니다.'),
        new OA\Property(property: 'details', type: 'object', nullable: true, additionalProperties: true),
    ]
)]
#[OA\Schema(
    schema: 'ApiSuccessEnvelope',
    type: 'object',
    required: ['success', 'data'],
    properties: [
        new OA\Property(property: 'success', type: 'boolean', example: true),
        new OA\Property(
            property: 'data',
            description: '엔드포인트별 실제 응답 데이터',
            oneOf: [
                new OA\Schema(type: 'object', additionalProperties: true),
                new OA\Schema(type: 'array', items: new OA\Items()),
                new OA\Schema(type: 'string'),
                new OA\Schema(type: 'integer'),
                new OA\Schema(type: 'number'),
                new OA\Schema(type: 'boolean'),
            ],
            nullable: true,
        ),
        new OA\Property(property: 'meta', ref: '#/components/schemas/ApiMeta', nullable: true),
    ]
)]
#[OA\Schema(
    schema: 'ApiErrorEnvelope',
    type: 'object',
    required: ['success', 'error'],
    properties: [
        new OA\Property(property: 'success', type: 'boolean', example: false),
        new OA\Property(property: 'error', ref: '#/components/schemas/ApiError'),
        new OA\Property(property: 'meta', ref: '#/components/schemas/ApiMeta', nullable: true),
    ]
)]
final class ApiResponseSchemas
{
}
