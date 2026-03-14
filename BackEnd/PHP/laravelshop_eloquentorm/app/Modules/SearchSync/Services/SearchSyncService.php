<?php

namespace App\Modules\SearchSync\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\SearchIndexOutbox;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class SearchSyncService
{
    public function summary(User $user): array
    {
        $this->assertAdmin($user);

        return [
            'total' => SearchIndexOutbox::query()->count(),
            'pending' => SearchIndexOutbox::query()->where('status', 'PENDING')->count(),
            'failed' => SearchIndexOutbox::query()->where('status', 'FAILED')->count(),
            'completed' => SearchIndexOutbox::query()->where('status', 'COMPLETED')->count(),
        ];
    }

    public function requeueFailed(User $user, int $limit): array
    {
        $this->assertAdmin($user);
        $items = SearchIndexOutbox::query()->where('status', 'FAILED')->limit($limit)->get();
        foreach ($items as $item) {
            $item->forceFill(['status' => 'PENDING', 'retry_count' => $item->retry_count + 1])->save();
        }

        return ['requeuedCount' => $items->count()];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
