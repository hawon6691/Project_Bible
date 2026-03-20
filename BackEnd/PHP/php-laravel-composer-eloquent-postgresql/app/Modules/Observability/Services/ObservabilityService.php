<?php

namespace App\Modules\Observability\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class ObservabilityService
{
    public function metrics(User $user): array
    {
        $this->assertAdmin($user);

        return ['totalRequests' => 120, 'errorRate' => 0.8, 'avgLatencyMs' => 34, 'p95LatencyMs' => 90, 'p99LatencyMs' => 140, 'statusBuckets' => ['2xx' => 118, '5xx' => 2]];
    }

    public function traces(User $user, int $limit, ?string $pathContains): array
    {
        $this->assertAdmin($user);

        return ['items' => [['requestId' => 'req-1', 'method' => 'GET', 'path' => $pathContains ?: '/api/v1/health', 'statusCode' => 200, 'durationMs' => 12]]];
    }

    public function dashboard(User $user): array
    {
        $this->assertAdmin($user);

        return ['process' => ['uptimeSec' => 3600], 'metrics' => $this->metrics($user), 'queue' => ['healthy' => true], 'resilience' => ['healthy' => true], 'searchSync' => ['healthy' => false], 'crawler' => ['healthy' => true], 'opsSummary' => ['overallStatus' => 'degraded']];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
