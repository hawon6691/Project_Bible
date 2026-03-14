<?php

declare(strict_types=1);

namespace App\OpenApi\Paths;

use OpenApi\Attributes as OA;

final class CommerceApiPaths
{
    #[OA\Get(
        path: '/api/v1/specs/definitions',
        tags: ['Spec'],
        summary: '스펙 정의 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [
            new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 401, description: 'Unauthorized', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Post(
        path: '/api/v1/specs/definitions',
        tags: ['Spec'],
        summary: '스펙 정의 생성',
        security: [['bearerAuth' => []]],
        responses: [
            new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 401, description: 'Unauthorized', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
            new OA\Response(response: 403, description: 'Forbidden', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Patch(
        path: '/api/v1/specs/definitions/{id}',
        tags: ['Spec'],
        summary: '스펙 정의 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [
            new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 404, description: 'Not Found', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Delete(
        path: '/api/v1/specs/definitions/{id}',
        tags: ['Spec'],
        summary: '스펙 정의 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [
            new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 404, description: 'Not Found', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Get(
        path: '/api/v1/products/{id}/specs',
        tags: ['Spec'],
        summary: '상품 스펙 조회',
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Put(
        path: '/api/v1/products/{id}/specs',
        tags: ['Spec'],
        summary: '상품 스펙 일괄 갱신',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [
            new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 403, description: 'Forbidden', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Get(
        path: '/api/v1/sellers',
        tags: ['Seller'],
        summary: '판매처 목록 조회',
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/sellers/{id}',
        tags: ['Seller'],
        summary: '판매처 상세 조회',
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [
            new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 404, description: 'Not Found', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Post(
        path: '/api/v1/sellers',
        tags: ['Seller'],
        summary: '판매처 생성',
        security: [['bearerAuth' => []]],
        responses: [
            new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope')),
            new OA\Response(response: 403, description: 'Forbidden', content: new OA\JsonContent(ref: '#/components/schemas/ApiErrorEnvelope')),
        ]
    )]
    #[OA\Patch(
        path: '/api/v1/sellers/{id}',
        tags: ['Seller'],
        summary: '판매처 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/sellers/{id}',
        tags: ['Seller'],
        summary: '판매처 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/products/{id}/prices',
        tags: ['Price'],
        summary: '상품 가격 목록 조회',
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/products/{id}/prices',
        tags: ['Price'],
        summary: '상품 가격 등록',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/prices/{id}',
        tags: ['Price'],
        summary: '가격 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/prices/{id}',
        tags: ['Price'],
        summary: '가격 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/products/{id}/price-history',
        tags: ['Price'],
        summary: '가격 이력 조회',
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/price-alerts',
        tags: ['Price'],
        summary: '가격 알림 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/price-alerts',
        tags: ['Price'],
        summary: '가격 알림 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/price-alerts/{id}',
        tags: ['Price'],
        summary: '가격 알림 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/cart',
        tags: ['Cart'],
        summary: '장바구니 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/cart',
        tags: ['Cart'],
        summary: '장바구니 항목 추가',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/cart/{itemId}',
        tags: ['Cart'],
        summary: '장바구니 항목 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'itemId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/cart/{itemId}',
        tags: ['Cart'],
        summary: '장바구니 항목 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'itemId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/cart',
        tags: ['Cart'],
        summary: '장바구니 전체 비우기',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'Cleared', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/addresses',
        tags: ['Address'],
        summary: '배송지 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/addresses',
        tags: ['Address'],
        summary: '배송지 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/addresses/{id}',
        tags: ['Address'],
        summary: '배송지 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/addresses/{id}',
        tags: ['Address'],
        summary: '배송지 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/orders',
        tags: ['Order'],
        summary: '주문 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/orders',
        tags: ['Order'],
        summary: '내 주문 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/orders/{id}',
        tags: ['Order'],
        summary: '주문 상세 조회',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/orders/{id}/cancel',
        tags: ['Order'],
        summary: '주문 취소',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Cancelled', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/admin/orders',
        tags: ['Order'],
        summary: '관리자 주문 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/admin/orders/{id}/status',
        tags: ['Order'],
        summary: '관리자 주문 상태 변경',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/payments',
        tags: ['Payment'],
        summary: '결제 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/payments/{id}',
        tags: ['Payment'],
        summary: '결제 상세 조회',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/payments/{id}/refund',
        tags: ['Payment'],
        summary: '결제 환불',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Refunded', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/products/{productId}/reviews',
        tags: ['Review'],
        summary: '상품 리뷰 목록 조회',
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/products/{productId}/reviews',
        tags: ['Review'],
        summary: '상품 리뷰 작성',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/reviews/{id}',
        tags: ['Review'],
        summary: '리뷰 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/reviews/{id}',
        tags: ['Review'],
        summary: '리뷰 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/wishlist',
        tags: ['Wishlist'],
        summary: '위시리스트 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/wishlist/{productId}',
        tags: ['Wishlist'],
        summary: '위시리스트 추가 또는 토글',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Toggled', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/wishlist/{productId}',
        tags: ['Wishlist'],
        summary: '위시리스트 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/points/balance',
        tags: ['Point'],
        summary: '포인트 잔액 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/points/transactions',
        tags: ['Point'],
        summary: '포인트 거래 내역 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/admin/points/grant',
        tags: ['Point'],
        summary: '관리자 포인트 지급',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Granted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    public function paths(): void {}
}
