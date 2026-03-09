<?php

namespace App\Modules\Friend\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\FriendActivity;
use App\Models\FriendBlock;
use App\Models\Friendship;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class FriendService
{
    public function request(User $actor, int $userId): array
    {
        if ($actor->id === $userId) {
            throw new BusinessException('본인에게 친구 요청을 보낼 수 없습니다.', 'FRIEND_REQUEST_INVALID', Response::HTTP_BAD_REQUEST);
        }

        $target = User::query()->find($userId);
        if (! $target) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        if ($this->isBlocked($actor->id, $userId) || $this->isBlocked($userId, $actor->id)) {
            throw new BusinessException('차단 관계에서는 친구 요청을 보낼 수 없습니다.', 'FRIEND_BLOCKED', Response::HTTP_BAD_REQUEST);
        }

        Friendship::query()->firstOrCreate([
            'requester_id' => $actor->id,
            'addressee_id' => $userId,
        ], [
            'status' => 'PENDING',
        ]);

        return ['message' => '친구 요청을 보냈습니다.'];
    }

    public function accept(User $actor, int $friendshipId): array
    {
        $friendship = Friendship::query()->find($friendshipId);
        if (! $friendship || $friendship->addressee_id !== $actor->id) {
            throw new BusinessException('친구 요청을 찾을 수 없습니다.', 'FRIEND_REQUEST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $friendship->forceFill(['status' => 'ACCEPTED'])->save();
        FriendActivity::query()->create([
            'user_id' => $actor->id,
            'type' => 'FRIEND_ACCEPTED',
            'message' => '친구 요청을 수락했습니다.',
        ]);

        return ['message' => '친구 요청을 수락했습니다.'];
    }

    public function reject(User $actor, int $friendshipId): array
    {
        $friendship = Friendship::query()->find($friendshipId);
        if (! $friendship || $friendship->addressee_id !== $actor->id) {
            throw new BusinessException('친구 요청을 찾을 수 없습니다.', 'FRIEND_REQUEST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $friendship->forceFill(['status' => 'REJECTED'])->save();

        return ['message' => '친구 요청을 거절했습니다.'];
    }

    public function list(User $actor, int $page, int $limit): array
    {
        $result = Friendship::query()
            ->where('status', 'ACCEPTED')
            ->where(function ($q) use ($actor): void {
                $q->where('requester_id', $actor->id)->orWhere('addressee_id', $actor->id);
            })
            ->orderByDesc('id')
            ->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (Friendship $friendship): array => $this->serializeFriendship($actor, $friendship))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function received(User $actor, int $page, int $limit): array
    {
        $result = Friendship::query()->where('addressee_id', $actor->id)->where('status', 'PENDING')->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (Friendship $friendship): array => $this->serializeRequest($friendship))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function sent(User $actor, int $page, int $limit): array
    {
        $result = Friendship::query()->where('requester_id', $actor->id)->where('status', 'PENDING')->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (Friendship $friendship): array => $this->serializeRequest($friendship))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function feed(User $actor, int $page, int $limit): array
    {
        $friendIds = Friendship::query()
            ->where('status', 'ACCEPTED')
            ->where(function ($q) use ($actor): void {
                $q->where('requester_id', $actor->id)->orWhere('addressee_id', $actor->id);
            })
            ->get()
            ->flatMap(fn (Friendship $f) => [$f->requester_id, $f->addressee_id])
            ->filter(fn ($id) => $id !== $actor->id)
            ->unique()
            ->values();

        $result = FriendActivity::query()->whereIn('user_id', $friendIds)->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (FriendActivity $activity): array => [
                'id' => $activity->id,
                'userId' => $activity->user_id,
                'type' => $activity->type,
                'message' => $activity->message,
                'createdAt' => optional($activity->created_at)?->toISOString(),
            ])->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function block(User $actor, int $userId): array
    {
        FriendBlock::query()->firstOrCreate([
            'user_id' => $actor->id,
            'blocked_user_id' => $userId,
        ]);

        Friendship::query()
            ->where(function ($q) use ($actor, $userId): void {
                $q->where('requester_id', $actor->id)->where('addressee_id', $userId);
            })
            ->orWhere(function ($q) use ($actor, $userId): void {
                $q->where('requester_id', $userId)->where('addressee_id', $actor->id);
            })
            ->delete();

        return ['message' => '사용자를 차단했습니다.'];
    }

    public function unblock(User $actor, int $userId): array
    {
        FriendBlock::query()->where('user_id', $actor->id)->where('blocked_user_id', $userId)->delete();

        return ['message' => '사용자 차단을 해제했습니다.'];
    }

    public function remove(User $actor, int $userId): array
    {
        Friendship::query()
            ->where('status', 'ACCEPTED')
            ->where(function ($q) use ($actor, $userId): void {
                $q->where('requester_id', $actor->id)->where('addressee_id', $userId);
            })
            ->orWhere(function ($q) use ($actor, $userId): void {
                $q->where('requester_id', $userId)->where('addressee_id', $actor->id);
            })
            ->delete();

        return ['message' => '친구를 삭제했습니다.'];
    }

    private function isBlocked(int $userId, int $targetId): bool
    {
        return FriendBlock::query()->where('user_id', $userId)->where('blocked_user_id', $targetId)->exists();
    }

    private function serializeFriendship(User $actor, Friendship $friendship): array
    {
        $friendId = $friendship->requester_id === $actor->id ? $friendship->addressee_id : $friendship->requester_id;
        $friend = User::query()->find($friendId);

        return [
            'id' => $friendship->id,
            'friend' => $friend ? [
                'id' => $friend->id,
                'name' => $friend->name,
                'nickname' => $friend->nickname,
            ] : null,
            'status' => $friendship->status,
            'createdAt' => optional($friendship->created_at)?->toISOString(),
        ];
    }

    private function serializeRequest(Friendship $friendship): array
    {
        return [
            'id' => $friendship->id,
            'requesterId' => $friendship->requester_id,
            'addresseeId' => $friendship->addressee_id,
            'status' => $friendship->status,
            'createdAt' => optional($friendship->created_at)?->toISOString(),
        ];
    }

    private function pagination($result): array
    {
        return [
            'page' => $result->currentPage(),
            'limit' => $result->perPage(),
            'total' => $result->total(),
            'totalPages' => $result->lastPage(),
        ];
    }
}
