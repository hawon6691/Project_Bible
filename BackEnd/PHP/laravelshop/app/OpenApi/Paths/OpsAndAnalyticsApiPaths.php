<?php

declare(strict_types=1);

namespace App\OpenApi\Paths;

use OpenApi\Attributes as OA;

final class OpsAndAnalyticsApiPaths
{
    #[OA\Get(
        path: '/api/v1/predictions/products/{productId}/price-trend',
        tags: ['Prediction'],
        summary: '상품 가격 추세 예측',
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/deals',
        tags: ['Deal'],
        summary: '특가 목록 조회',
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/deals/admin',
        tags: ['Deal'],
        summary: '특가 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/deals/admin/{id}',
        tags: ['Deal'],
        summary: '특가 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/deals/admin/{id}',
        tags: ['Deal'],
        summary: '특가 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/recommendations/trending',
        tags: ['Recommendation'],
        summary: '트렌딩 추천 조회',
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/recommendations/personal',
        tags: ['Recommendation'],
        summary: '개인화 추천 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/admin/recommendations',
        tags: ['Recommendation'],
        summary: '관리자 추천 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/admin/recommendations',
        tags: ['Recommendation'],
        summary: '관리자 추천 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/admin/recommendations/{id}',
        tags: ['Recommendation'],
        summary: '관리자 추천 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/rankings/products/popular',
        tags: ['Ranking'],
        summary: '인기 상품 랭킹 조회',
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/rankings/keywords/popular',
        tags: ['Ranking'],
        summary: '인기 키워드 랭킹 조회',
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/rankings/admin/recalculate',
        tags: ['Ranking'],
        summary: '랭킹 재계산',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'Recalculated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(path: '/api/v1/fraud/alerts', tags: ['Fraud'], summary: '사기 알림 목록 조회', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Patch(path: '/api/v1/fraud/alerts/{id}/approve', tags: ['Fraud'], summary: '사기 알림 승인', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Approved', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Patch(path: '/api/v1/fraud/alerts/{id}/reject', tags: ['Fraud'], summary: '사기 알림 반려', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Rejected', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/products/{id}/real-price', tags: ['Fraud'], summary: '실구매가 조회', parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/fraud/products/{productId}/effective-prices', tags: ['Fraud'], summary: '유효 가격 조회', parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/fraud/products/{productId}/anomalies', tags: ['Fraud'], summary: '가격 이상 탐지 조회', parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/fraud/admin/products/{productId}/scan', tags: ['Fraud'], summary: '관리자 사기 스캔 실행', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Scanned', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/fraud/admin/products/{productId}/flags', tags: ['Fraud'], summary: '관리자 사기 플래그 조회', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/trust/sellers/{sellerId}', tags: ['Trust'], summary: '판매처 신뢰도 조회', parameters: [new OA\Parameter(name: 'sellerId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/trust/sellers/{sellerId}/history', tags: ['Trust'], summary: '판매처 신뢰도 이력 조회', parameters: [new OA\Parameter(name: 'sellerId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/trust/admin/sellers/{sellerId}/recalculate', tags: ['Trust'], summary: '판매처 신뢰도 재계산', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'sellerId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Recalculated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/i18n/translations', tags: ['I18n'], summary: '번역 목록 조회', responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/i18n/admin/translations', tags: ['I18n'], summary: '번역 저장', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 201, description: 'Saved', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/i18n/admin/translations/{id}', tags: ['I18n'], summary: '번역 삭제', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/i18n/exchange-rates', tags: ['I18n'], summary: '환율 목록 조회', responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/i18n/admin/exchange-rates', tags: ['I18n'], summary: '환율 저장', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 201, description: 'Saved', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/i18n/convert', tags: ['I18n'], summary: '통화 변환', responses: [new OA\Response(response: 200, description: 'Converted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/images/upload', tags: ['Image'], summary: '이미지 업로드', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 201, description: 'Uploaded', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/images/{id}/variants', tags: ['Image'], summary: '이미지 변환본 조회', parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/images/{id}', tags: ['Image'], summary: '이미지 삭제', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/badges', tags: ['Badge'], summary: '배지 목록 조회', responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/badges/me', tags: ['Badge'], summary: '내 배지 조회', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/users/{id}/badges', tags: ['Badge'], summary: '사용자 배지 조회', parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/admin/badges', tags: ['Badge'], summary: '배지 생성', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Patch(path: '/api/v1/admin/badges/{id}', tags: ['Badge'], summary: '배지 수정', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/admin/badges/{id}', tags: ['Badge'], summary: '배지 삭제', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/admin/badges/{id}/grant', tags: ['Badge'], summary: '배지 수여', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 201, description: 'Granted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/admin/badges/{id}/revoke/{userId}', tags: ['Badge'], summary: '배지 회수', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')), new OA\Parameter(name: 'userId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Revoked', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/pc-builds', tags: ['PcBuilder'], summary: 'PC 견적 목록 조회', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/pc-builds', tags: ['PcBuilder'], summary: 'PC 견적 생성', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/pc-builds/{id}', tags: ['PcBuilder'], summary: 'PC 견적 상세 조회', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Patch(path: '/api/v1/pc-builds/{id}', tags: ['PcBuilder'], summary: 'PC 견적 수정', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/pc-builds/{id}', tags: ['PcBuilder'], summary: 'PC 견적 삭제', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/pc-builds/{id}/parts', tags: ['PcBuilder'], summary: 'PC 견적 부품 추가', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 201, description: 'Added', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/pc-builds/{id}/parts/{partId}', tags: ['PcBuilder'], summary: 'PC 견적 부품 삭제', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')), new OA\Parameter(name: 'partId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/pc-builds/{id}/compatibility-check', tags: ['PcBuilder'], summary: 'PC 견적 호환성 검사', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Checked', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/pc-builds/{id}/share', tags: ['PcBuilder'], summary: 'PC 견적 공유 링크 발급', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Shared', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/pc-builds/shared/{token}', tags: ['PcBuilder'], summary: '공유 PC 견적 조회', parameters: [new OA\Parameter(name: 'token', in: 'path', required: true, schema: new OA\Schema(type: 'string'))], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/pc-builds/popular', tags: ['PcBuilder'], summary: '인기 PC 견적 조회', responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Get(path: '/api/v1/admin/pc-compatibility-rules', tags: ['PcBuilder'], summary: '관리자 호환성 규칙 목록', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Post(path: '/api/v1/admin/pc-compatibility-rules', tags: ['PcBuilder'], summary: '관리자 호환성 규칙 생성', security: [['bearerAuth' => []]], responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Patch(path: '/api/v1/admin/pc-compatibility-rules/{id}', tags: ['PcBuilder'], summary: '관리자 호환성 규칙 수정', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    #[OA\Delete(path: '/api/v1/admin/pc-compatibility-rules/{id}', tags: ['PcBuilder'], summary: '관리자 호환성 규칙 삭제', security: [['bearerAuth' => []]], parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))], responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))])]
    public function paths(): void {}
}
