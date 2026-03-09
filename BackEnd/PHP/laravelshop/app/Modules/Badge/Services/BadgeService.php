<?php

namespace App\Modules\Badge\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Badge;
use App\Models\User;
use App\Models\UserBadge;
use Symfony\Component\HttpFoundation\Response;

class BadgeService
{
    public function all(): array
    {
        return Badge::query()->withCount('userBadges')->orderBy('id')->get()
            ->map(fn (Badge $badge): array => $this->serializeBadge($badge, $badge->user_badges_count))
            ->values()->all();
    }

    public function myBadges(User $user): array
    {
        return $this->userBadges($user->id);
    }

    public function userBadges(int $userId): array
    {
        return UserBadge::query()->with('badge')->where('user_id', $userId)->orderByDesc('granted_at')->get()
            ->map(fn (UserBadge $userBadge): array => [
                'id' => $userBadge->id,
                'grantedAt' => optional($userBadge->granted_at)?->toISOString(),
                'badge' => $userBadge->badge ? $this->serializeBadge($userBadge->badge) : null,
            ])->values()->all();
    }

    public function create(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);
        $badge = Badge::query()->create([
            'name' => $payload['name'],
            'description' => $payload['description'] ?? null,
            'icon_url' => $payload['iconUrl'] ?? null,
            'type' => strtoupper($payload['type'] ?? 'AUTO'),
            'condition' => $payload['condition'] ?? null,
            'rarity' => strtoupper($payload['rarity'] ?? 'COMMON'),
        ]);

        return $this->serializeBadge($badge);
    }

    public function update(User $actor, int $badgeId, array $payload): array
    {
        $this->assertAdmin($actor);
        $badge = $this->findBadge($badgeId);
        $updates = [];
        if (array_key_exists('name', $payload)) {
            $updates['name'] = $payload['name'];
        }
        if (array_key_exists('description', $payload)) {
            $updates['description'] = $payload['description'];
        }
        if (array_key_exists('iconUrl', $payload)) {
            $updates['icon_url'] = $payload['iconUrl'];
        }
        if (array_key_exists('type', $payload)) {
            $updates['type'] = strtoupper($payload['type']);
        }
        if (array_key_exists('condition', $payload)) {
            $updates['condition'] = $payload['condition'];
        }
        if (array_key_exists('rarity', $payload)) {
            $updates['rarity'] = strtoupper($payload['rarity']);
        }
        if ($updates !== []) {
            $badge->forceFill($updates)->save();
        }

        return $this->serializeBadge($badge);
    }

    public function remove(User $actor, int $badgeId): array
    {
        $this->assertAdmin($actor);
        $badge = $this->findBadge($badgeId);
        $badge->delete();

        return ['message' => '배지가 삭제되었습니다.'];
    }

    public function grant(User $actor, int $badgeId, int $userId): array
    {
        $this->assertAdmin($actor);
        $this->findBadge($badgeId);
        $granted = UserBadge::query()->firstOrCreate(
            ['badge_id' => $badgeId, 'user_id' => $userId],
            ['granted_by' => $actor->id, 'granted_at' => now()],
        );
        $granted->load('badge');

        return [
            'id' => $granted->id,
            'grantedAt' => optional($granted->granted_at)?->toISOString(),
            'badge' => $granted->badge ? $this->serializeBadge($granted->badge) : null,
            'userId' => $granted->user_id,
        ];
    }

    public function revoke(User $actor, int $badgeId, int $userId): array
    {
        $this->assertAdmin($actor);
        UserBadge::query()->where('badge_id', $badgeId)->where('user_id', $userId)->delete();

        return ['message' => '배지가 회수되었습니다.'];
    }

    private function findBadge(int $badgeId): Badge
    {
        $badge = Badge::query()->find($badgeId);
        if (! $badge) {
            throw new BusinessException('배지를 찾을 수 없습니다.', 'BADGE_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $badge;
    }

    private function assertAdmin(User $actor): void
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }

    private function serializeBadge(Badge $badge, ?int $holderCount = null): array
    {
        return [
            'id' => $badge->id,
            'name' => $badge->name,
            'description' => $badge->description,
            'iconUrl' => $badge->icon_url,
            'type' => $badge->type,
            'condition' => $badge->condition,
            'rarity' => $badge->rarity,
            'holderCount' => $holderCount ?? $badge->userBadges()->count(),
        ];
    }
}
