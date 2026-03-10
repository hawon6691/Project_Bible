<?php

namespace App\Http\Controllers\Api\V1\Product;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Product\Requests\ListProductsRequest;
use App\Modules\Product\Requests\StoreProductRequest;
use App\Modules\Product\Requests\UpdateProductRequest;
use App\Modules\Product\Services\ProductService;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Products')]
class ProductController extends ApiController
{
    public function __construct(
        private readonly ProductService $productService,
    ) {}

    #[OA\Get(
        path: '/api/v1/products',
        operationId: 'productIndex',
        summary: '상품 목록 조회',
        tags: ['Products'],
        parameters: [
            new OA\Parameter(name: 'search', in: 'query', required: false, schema: new OA\Schema(type: 'string')),
            new OA\Parameter(name: 'categoryId', in: 'query', required: false, schema: new OA\Schema(type: 'integer')),
            new OA\Parameter(name: 'page', in: 'query', required: false, schema: new OA\Schema(type: 'integer', default: 1)),
            new OA\Parameter(name: 'limit', in: 'query', required: false, schema: new OA\Schema(type: 'integer', default: 20)),
        ],
        responses: [new OA\Response(response: 200, description: '목록 조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function index(ListProductsRequest $request)
    {
        return $this->success(
            $this->productService->list($request->validated()),
            status: 200
        );
    }

    #[OA\Get(
        path: '/api/v1/products/{id}',
        operationId: 'productShow',
        summary: '상품 상세 조회',
        tags: ['Products'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '상세 조회 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function show(int $id)
    {
        return $this->success($this->productService->detail($id));
    }

    #[OA\Post(
        path: '/api/v1/products',
        operationId: 'productStore',
        summary: '상품 등록',
        security: [['bearerAuth' => []]],
        tags: ['Products'],
        responses: [new OA\Response(response: 201, description: '등록 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function store(StoreProductRequest $request)
    {
        return $this->success(
            $this->productService->store($request->user(), $request->validated()),
            status: 201
        );
    }

    #[OA\Patch(
        path: '/api/v1/products/{id}',
        operationId: 'productUpdate',
        summary: '상품 수정',
        security: [['bearerAuth' => []]],
        tags: ['Products'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '수정 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function update(UpdateProductRequest $request, int $id)
    {
        return $this->success($this->productService->update($request->user(), $id, $request->validated()));
    }

    #[OA\Delete(
        path: '/api/v1/products/{id}',
        operationId: 'productDestroy',
        summary: '상품 삭제',
        security: [['bearerAuth' => []]],
        tags: ['Products'],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer', example: 1)),
        ],
        responses: [new OA\Response(response: 200, description: '삭제 성공', content: new OA\JsonContent(ref: '#/components/schemas/ApiSuccessEnvelope'))],
    )]
    public function destroy(Request $request, int $id)
    {
        return $this->success($this->productService->delete($request->user(), $id));
    }
}
