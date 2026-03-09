<?php

namespace App\Http\Controllers\Api\V1\Community;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Community\Requests\StoreCommentRequest;
use App\Modules\Community\Requests\StorePostRequest;
use App\Modules\Community\Requests\UpdatePostRequest;
use App\Modules\Community\Services\CommunityService;

class CommunityController extends ApiController
{
    public function __construct(
        private readonly CommunityService $communityService,
    ) {
    }

    public function boards()
    {
        return $this->success($this->communityService->listBoards());
    }

    public function posts(int $boardId)
    {
        return $this->success($this->communityService->listPosts($boardId));
    }

    public function showPost(int $id)
    {
        return $this->success($this->communityService->showPost($id));
    }

    public function storePost(StorePostRequest $request, int $boardId)
    {
        return $this->success($this->communityService->createPost($request->user(), $boardId, $request->validated()), status: 201);
    }

    public function updatePost(UpdatePostRequest $request, int $id)
    {
        return $this->success($this->communityService->updatePost($request->user(), $id, $request->validated()));
    }

    public function destroyPost(int $id)
    {
        return $this->success($this->communityService->deletePost(request()->user(), $id));
    }

    public function toggleLike(int $id)
    {
        return $this->success($this->communityService->toggleLike(request()->user(), $id));
    }

    public function comments(int $id)
    {
        return $this->success($this->communityService->listComments($id));
    }

    public function storeComment(StoreCommentRequest $request, int $id)
    {
        return $this->success($this->communityService->createComment($request->user(), $id, $request->validated()), status: 201);
    }

    public function destroyComment(int $id)
    {
        return $this->success($this->communityService->deleteComment(request()->user(), $id));
    }
}
