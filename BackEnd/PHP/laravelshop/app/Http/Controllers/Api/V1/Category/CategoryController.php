<?php

namespace App\Http\Controllers\Api\V1\Category;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Category\Requests\CreateCategoryRequest;
use App\Modules\Category\Requests\UpdateCategoryRequest;
use App\Modules\Category\Services\CategoryService;
use Illuminate\Http\Request;

class CategoryController extends ApiController
{
    public function __construct(
        private readonly CategoryService $categoryService,
    ) {}

    public function index()
    {
        return $this->success($this->categoryService->listTree());
    }

    public function show(int $id)
    {
        return $this->success($this->categoryService->getById($id));
    }

    public function store(CreateCategoryRequest $request)
    {
        return $this->success(
            $this->categoryService->create($request->user(), $request->validated()),
            status: 201
        );
    }

    public function update(UpdateCategoryRequest $request, int $id)
    {
        return $this->success($this->categoryService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->categoryService->delete($request->user(), $id));
    }
}
