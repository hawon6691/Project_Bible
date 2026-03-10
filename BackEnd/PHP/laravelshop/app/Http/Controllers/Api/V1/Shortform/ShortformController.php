<?php

namespace App\Http\Controllers\Api\V1\Shortform;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Shortform\Requests\StoreShortformCommentRequest;
use App\Modules\Shortform\Requests\StoreShortformRequest;
use App\Modules\Shortform\Services\ShortformService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Shortform')]
class ShortformController extends ApiController
{
    public function __construct(private readonly ShortformService $service) {}

    public function store(StoreShortformRequest $request)
    {
        return $this->success($this->service->create($request->user(), $request->validated()), [], 201);
    }

    public function index(Request $request)
    {
        return $this->success($this->service->list($request->query('cursor'), (int) $request->query('limit', 20)));
    }

    public function show(int $id)
    {
        return $this->success($this->service->show($id));
    }

    public function like(Request $request, int $id)
    {
        return $this->success($this->service->toggleLike($request->user(), $id));
    }

    public function storeComment(StoreShortformCommentRequest $request, int $id)
    {
        return $this->success($this->service->addComment($request->user(), $id, $request->validated()), [], 201);
    }

    public function comments(Request $request, int $id)
    {
        return $this->success($this->service->comments($id, (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function ranking(Request $request)
    {
        return $this->success($this->service->ranking((string) $request->query('period', 'day'), (int) $request->query('limit', 20)));
    }

    public function transcodeStatus(int $id)
    {
        return $this->success($this->service->transcodeStatus($id));
    }

    public function retry(Request $request, int $id)
    {
        return $this->success($this->service->retry($request->user(), $id));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->service->delete($request->user(), $id));
    }

    public function userShortforms(Request $request, int $userId)
    {
        return $this->success($this->service->byUser($userId, (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }
}
