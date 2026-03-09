<?php

namespace App\Http\Controllers\Api\V1\Friend;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Friend\Services\FriendService;
use Illuminate\Http\Request;

class FriendController extends ApiController
{
    public function __construct(private readonly FriendService $service)
    {
    }

    public function request(Request $request, int $userId)
    {
        return $this->success($this->service->request($request->user(), $userId), [], 201);
    }

    public function accept(Request $request, int $friendshipId)
    {
        return $this->success($this->service->accept($request->user(), $friendshipId));
    }

    public function reject(Request $request, int $friendshipId)
    {
        return $this->success($this->service->reject($request->user(), $friendshipId));
    }

    public function index(Request $request)
    {
        return $this->success($this->service->list($request->user(), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function received(Request $request)
    {
        return $this->success($this->service->received($request->user(), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function sent(Request $request)
    {
        return $this->success($this->service->sent($request->user(), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function feed(Request $request)
    {
        return $this->success($this->service->feed($request->user(), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function block(Request $request, int $userId)
    {
        return $this->success($this->service->block($request->user(), $userId), [], 201);
    }

    public function unblock(Request $request, int $userId)
    {
        return $this->success($this->service->unblock($request->user(), $userId));
    }

    public function destroy(Request $request, int $userId)
    {
        return $this->success($this->service->remove($request->user(), $userId));
    }
}
