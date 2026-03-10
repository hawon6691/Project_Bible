<?php

namespace App\Http\Controllers\Api\V1\User;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\User\Requests\UpdateMeRequest;
use App\Modules\User\Requests\UpdateProfileRequest;
use App\Modules\User\Requests\UpdateUserStatusRequest;
use App\Modules\User\Requests\UploadProfileImageRequest;
use App\Modules\User\Services\UserService;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Users')]
class UserController extends ApiController
{
    public function __construct(
        private readonly UserService $userService,
    ) {}

    #[OA\Get(
        path: '/api/v1/users/me',
        operationId: 'userMe',
        summary: '내 정보 조회',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        responses: [new OA\Response(response: 200, description: '조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function me(Request $request)
    {
        return $this->success($this->userService->getMe($request->user()));
    }

    #[OA\Patch(
        path: '/api/v1/users/me',
        operationId: 'userUpdateMe',
        summary: '내 정보 수정',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        responses: [new OA\Response(response: 200, description: '수정 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function updateMe(UpdateMeRequest $request)
    {
        return $this->success($this->userService->updateMe($request->user(), $request->validated()));
    }

    #[OA\Delete(
        path: '/api/v1/users/me',
        operationId: 'userDeleteMe',
        summary: '회원 탈퇴',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        responses: [new OA\Response(response: 200, description: '삭제 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function deleteMe(Request $request)
    {
        return $this->success($this->userService->deleteMe($request->user()));
    }

    #[OA\Get(
        path: '/api/v1/users/{id}/profile',
        operationId: 'userProfile',
        summary: '사용자 프로필 조회',
        tags: ['Users'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function profile(int $id)
    {
        return $this->success($this->userService->getProfile($id));
    }

    #[OA\Patch(
        path: '/api/v1/users/me/profile',
        operationId: 'userUpdateProfile',
        summary: '프로필 상세 수정',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        responses: [new OA\Response(response: 200, description: '수정 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function updateProfile(UpdateProfileRequest $request)
    {
        return $this->success($this->userService->updateProfile($request->user(), $request->validated()));
    }

    #[OA\Post(
        path: '/api/v1/users/me/profile-image',
        operationId: 'userUploadProfileImage',
        summary: '프로필 이미지 업로드',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\MediaType(
                mediaType: 'multipart/form-data',
                schema: new OA\Schema(
                    required: ['image'],
                    properties: [
                        new OA\Property(property: 'image', type: 'string', format: 'binary'),
                    ],
                ),
            ),
        ),
        responses: [new OA\Response(response: 200, description: '업로드 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function uploadProfileImage(UploadProfileImageRequest $request)
    {
        return $this->success($this->userService->uploadProfileImage($request->user(), $request->file('image')));
    }

    #[OA\Delete(
        path: '/api/v1/users/me/profile-image',
        operationId: 'userDeleteProfileImage',
        summary: '프로필 이미지 삭제',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        responses: [new OA\Response(response: 200, description: '삭제 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function deleteProfileImage(Request $request)
    {
        return $this->success($this->userService->deleteProfileImage($request->user()));
    }

    #[OA\Get(
        path: '/api/v1/users',
        operationId: 'userIndex',
        summary: '사용자 목록 조회',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        responses: [new OA\Response(response: 200, description: '목록 조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function index(Request $request)
    {
        return $this->success($this->userService->listUsers($request->user(), $request->query()));
    }

    #[OA\Patch(
        path: '/api/v1/users/{id}/status',
        operationId: 'userUpdateStatus',
        summary: '사용자 상태 변경',
        security: [['bearerAuth' => []]],
        tags: ['Users'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '상태 변경 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function updateStatus(UpdateUserStatusRequest $request, int $id)
    {
        return $this->success($this->userService->updateUserStatus(
            $request->user(),
            $id,
            $request->validated()['status'],
        ));
    }
}
