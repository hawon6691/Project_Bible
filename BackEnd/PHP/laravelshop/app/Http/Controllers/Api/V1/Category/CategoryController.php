<?php

namespace App\Http\Controllers\Api\V1\Category;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Category\Requests\CreateCategoryRequest;
use App\Modules\Category\Requests\UpdateCategoryRequest;
use App\Modules\Category\Services\CategoryService;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Categories')]
class CategoryController extends ApiController
{
    public function __construct(
        private readonly CategoryService $categoryService,
    ) {}

    #[OA\Get(
        path: '/api/v1/categories',
        operationId: 'categoryIndex',
        summary: '카테고리 트리 조회',
        tags: ['Categories'],
        responses: [new OA\Response(response: 200, description: '조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function index()
    {
        return $this->success($this->categoryService->listTree());
    }

    #[OA\Get(
        path: '/api/v1/categories/{id}',
        operationId: 'categoryShow',
        summary: '카테고리 상세 조회',
        tags: ['Categories'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '상세 조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function show(int $id)
    {
        return $this->success($this->categoryService->getById($id));
    }

    #[OA\Post(
        path: '/api/v1/categories',
        operationId: 'categoryStore',
        summary: '카테고리 생성',
        security: [['bearerAuth' => []]],
        tags: ['Categories'],
        responses: [new OA\Response(response: 201, description: '생성 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function store(CreateCategoryRequest $request)
    {
        return $this->success(
            $this->categoryService->create($request->user(), $request->validated()),
            status: 201
        );
    }

    #[OA\Patch(
        path: '/api/v1/categories/{id}',
        operationId: 'categoryUpdate',
        summary: '카테고리 수정',
        security: [['bearerAuth' => []]],
        tags: ['Categories'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '수정 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function update(UpdateCategoryRequest $request, int $id)
    {
        return $this->success($this->categoryService->update($request->user(), $id, $request->validated()));
    }

    #[OA\Delete(
        path: '/api/v1/categories/{id}',
        operationId: 'categoryDestroy',
        summary: '카테고리 삭제',
        security: [['bearerAuth' => []]],
        tags: ['Categories'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '삭제 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function destroy(Request $request, int $id)
    {
        return $this->success($this->categoryService->delete($request->user(), $id));
    }
}
