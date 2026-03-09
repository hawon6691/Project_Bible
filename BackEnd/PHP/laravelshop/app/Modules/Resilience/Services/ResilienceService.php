<?php

namespace App\Modules\Resilience\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class ResilienceService
{
    private function items(): array
    {
        return [
            ['name' => 'search-sync', 'state' => 'CLOSED', 'failureCount' => 0],
            ['name' => 'crawler', 'state' => 'HALF_OPEN', 'failureCount' => 1],
        ];
    }

    public function list(User $user): array
    {
        $this->assertAdmin($user);
        return ['items' => $this->items()];
    }

    public function policies(User $user): array
    {
        $this->assertAdmin($user);
        return ['items' => [['name' => 'crawler', 'options' => ['threshold' => 5], 'stats' => ['opened' => 1]]]];
    }

    public function show(User $user, string $name): array
    {
        $this->assertAdmin($user);
        return collect($this->items())->firstWhere('name', $name) ?? ['name' => $name, 'state' => 'CLOSED', 'failureCount' => 0];
    }

    public function reset(User $user, string $name): array
    {
        $this->assertAdmin($user);
        return ['message' => 'Circuit Breaker가 초기화되었습니다.', 'name' => $name];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
