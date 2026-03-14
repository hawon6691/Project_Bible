<?php

declare(strict_types=1);

namespace App\OpenApi\Paths;

use OpenApi\Attributes as OA;

final class CommunityMediaApiPaths
{
    #[OA\Get(
        path: '/api/v1/boards',
        tags: ['Community'],
        summary: '게시판 목록 조회',
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/boards/{boardId}/posts',
        tags: ['Community'],
        summary: '게시글 목록 조회',
        parameters: [new OA\Parameter(name: 'boardId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/boards/{boardId}/posts',
        tags: ['Community'],
        summary: '게시글 생성',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'boardId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/posts/{id}',
        tags: ['Community'],
        summary: '게시글 상세 조회',
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/posts/{id}',
        tags: ['Community'],
        summary: '게시글 수정',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/posts/{id}',
        tags: ['Community'],
        summary: '게시글 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/posts/{id}/like',
        tags: ['Community'],
        summary: '게시글 좋아요 토글',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Toggled', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/posts/{id}/comments',
        tags: ['Community'],
        summary: '댓글 목록 조회',
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/posts/{id}/comments',
        tags: ['Community'],
        summary: '댓글 생성',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/comments/{id}',
        tags: ['Community'],
        summary: '댓글 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/products/{productId}/inquiries',
        tags: ['Inquiry'],
        summary: '상품 문의 목록 조회',
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/products/{productId}/inquiries',
        tags: ['Inquiry'],
        summary: '상품 문의 작성',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/inquiries/me',
        tags: ['Inquiry'],
        summary: '내 문의 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/admin/inquiries/{id}/answer',
        tags: ['Inquiry'],
        summary: '관리자 문의 답변',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Answered', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/inquiries/{id}',
        tags: ['Inquiry'],
        summary: '문의 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/support/tickets',
        tags: ['Support'],
        summary: '내 고객센터 티켓 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/support/tickets',
        tags: ['Support'],
        summary: '고객센터 티켓 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/support/tickets/{id}',
        tags: ['Support'],
        summary: '고객센터 티켓 상세 조회',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/support/tickets/{id}/replies',
        tags: ['Support'],
        summary: '고객센터 티켓 답글 생성',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/admin/support/tickets',
        tags: ['Support'],
        summary: '관리자 티켓 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Patch(
        path: '/api/v1/admin/support/tickets/{id}/status',
        tags: ['Support'],
        summary: '관리자 티켓 상태 변경',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Updated', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/activities',
        tags: ['Activity'],
        summary: '활동 내역 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/activities/recent-products',
        tags: ['Activity'],
        summary: '최근 본 상품 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/activities/recent-products/{productId}',
        tags: ['Activity'],
        summary: '최근 본 상품 기록',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'productId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Recorded', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/activities/searches',
        tags: ['Activity'],
        summary: '검색 기록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/activities/searches',
        tags: ['Activity'],
        summary: '검색 기록 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Recorded', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/activities/searches/{id}',
        tags: ['Activity'],
        summary: '검색 기록 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/activities/searches',
        tags: ['Activity'],
        summary: '검색 기록 전체 삭제',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/chat/rooms',
        tags: ['Chat'],
        summary: '채팅방 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/chat/rooms',
        tags: ['Chat'],
        summary: '채팅방 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Created', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/chat/rooms/{id}/join',
        tags: ['Chat'],
        summary: '채팅방 참여',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Joined', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/chat/rooms/{id}/messages',
        tags: ['Chat'],
        summary: '채팅 메시지 목록 조회',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/chat/rooms/{id}/messages',
        tags: ['Chat'],
        summary: '채팅 메시지 전송',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 201, description: 'Sent', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/push/subscriptions',
        tags: ['Push'],
        summary: '푸시 구독 등록',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Subscribed', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/push/subscriptions/unsubscribe',
        tags: ['Push'],
        summary: '푸시 구독 해제',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'Unsubscribed', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/push/subscriptions',
        tags: ['Push'],
        summary: '푸시 구독 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/push/preferences',
        tags: ['Push'],
        summary: '푸시 설정 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/push/preferences',
        tags: ['Push'],
        summary: '푸시 설정 저장',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'Saved', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/friends',
        tags: ['Friend'],
        summary: '친구 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/friends/requests',
        tags: ['Friend'],
        summary: '친구 요청 목록 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/friends/requests',
        tags: ['Friend'],
        summary: '친구 요청 생성',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Requested', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/friends/requests/{id}/accept',
        tags: ['Friend'],
        summary: '친구 요청 수락',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Accepted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/friends/requests/{id}/reject',
        tags: ['Friend'],
        summary: '친구 요청 거절',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Rejected', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/friends/{id}',
        tags: ['Friend'],
        summary: '친구 삭제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Deleted', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Get(
        path: '/api/v1/friends/activities',
        tags: ['Friend'],
        summary: '친구 활동 피드 조회',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 200, description: 'OK', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Post(
        path: '/api/v1/friends/blocks',
        tags: ['Friend'],
        summary: '친구 차단',
        security: [['bearerAuth' => []]],
        responses: [new OA\Response(response: 201, description: 'Blocked', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    #[OA\Delete(
        path: '/api/v1/friends/blocks/{userId}',
        tags: ['Friend'],
        summary: '친구 차단 해제',
        security: [['bearerAuth' => []]],
        parameters: [new OA\Parameter(name: 'userId', in: 'path', required: true, schema: new OA\Schema(type: 'integer'))],
        responses: [new OA\Response(response: 200, description: 'Unblocked', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))]
    )]
    public function paths(): void {}
}
