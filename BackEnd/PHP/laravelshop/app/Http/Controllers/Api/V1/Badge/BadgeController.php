<?php

namespace App\Http\Controllers\Api\V1\Badge;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Badge\Requests\CreateBadgeRequest;
use App\Modules\Badge\Requests\GrantBadgeRequest;
use App\Modules\Badge\Requests\UpdateBadgeRequest;
use App\Modules\Badge\Services\BadgeService;

class BadgeController extends ApiController
{
    public function __construct(
        private readonly BadgeService $badgeService,
    ) {
    }

    public function index()
    {
        return $this->success($this->badgeService->all());
    }

    public function me()
    {
        return $this->success($this->badgeService->myBadges(request()->user()));
    }

    public function userBadges(int $id)
    {
        return $this->success($this->badgeService->userBadges($id));
    }

    public function store(CreateBadgeRequest $request)
    {
        return $this->success($this->badgeService->create($request->user(), $request->validated()), status: 201);
    }

    public function update(UpdateBadgeRequest $request, int $id)
    {
        return $this->success($this->badgeService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(int $id)
    {
        return $this->success($this->badgeService->remove(request()->user(), $id));
    }

    public function grant(GrantBadgeRequest $request, int $id)
    {
        return $this->success($this->badgeService->grant($request->user(), $id, (int) $request->validated()['userId']), status: 201);
    }

    public function revoke(int $id, int $userId)
    {
        return $this->success($this->badgeService->revoke(request()->user(), $id, $userId));
    }
}
