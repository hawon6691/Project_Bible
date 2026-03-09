<?php

namespace App\Http\Controllers\Api\V1\News;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\News\Requests\StoreNewsCategoryRequest;
use App\Modules\News\Requests\StoreNewsRequest;
use App\Modules\News\Requests\UpdateNewsRequest;
use App\Modules\News\Services\NewsService;
use Illuminate\Http\Request;

class NewsController extends ApiController
{
    public function __construct(private readonly NewsService $service)
    {
    }

    public function index(Request $request)
    {
        return $this->success($this->service->list($request->query('category') ? (int) $request->query('category') : null, (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function categories()
    {
        return $this->success($this->service->categories());
    }

    public function show(int $id)
    {
        return $this->success($this->service->show($id));
    }

    public function store(StoreNewsRequest $request)
    {
        return $this->success($this->service->create($request->user(), $request->validated()), [], 201);
    }

    public function update(UpdateNewsRequest $request, int $id)
    {
        return $this->success($this->service->update($request->user(), $id, $request->validated()));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->service->delete($request->user(), $id));
    }

    public function storeCategory(StoreNewsCategoryRequest $request)
    {
        return $this->success($this->service->createCategory($request->user(), $request->validated()), [], 201);
    }

    public function destroyCategory(Request $request, int $id)
    {
        return $this->success($this->service->deleteCategory($request->user(), $id));
    }
}
