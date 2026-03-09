<?php

namespace App\Modules\QueueAdmin\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class QueueAdminService
{
    public function supported(User $user): array
    {
        $this->assertAdmin($user);
        return ['items' => ['default', 'crawler', 'search-sync']];
    }

    public function stats(User $user): array
    {
        $this->assertAdmin($user);
        return ['total' => 3, 'items' => [
            ['queueName' => 'default', 'paused' => false, 'counts' => ['waiting' => 1, 'failed' => 0]],
            ['queueName' => 'crawler', 'paused' => false, 'counts' => ['waiting' => 2, 'failed' => 1]],
        ]];
    }

    public function failed(User $user, string $queueName, int $page, int $limit): array
    {
        $this->assertAdmin($user);
        return ['items' => [[ 'jobId' => 'failed-1', 'queueName' => $queueName, 'status' => 'failed' ]], 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => 1, 'totalPages' => 1]];
    }

    public function retryFailed(User $user, string $queueName, int $limit): array
    {
        $this->assertAdmin($user);
        return ['requested' => $limit, 'requeuedCount' => min($limit, 1), 'jobIds' => ['failed-1']];
    }

    public function autoRetry(User $user, int $perQueueLimit, int $maxTotal): array
    {
        $this->assertAdmin($user);
        return ['retriedTotal' => min($maxTotal, $perQueueLimit), 'items' => [['queueName' => 'crawler', 'retriedCount' => 1]]];
    }

    public function retryJob(User $user, string $queueName, string $jobId): array
    {
        $this->assertAdmin($user);
        return ['retried' => true, 'queueName' => $queueName, 'jobId' => $jobId];
    }

    public function removeJob(User $user, string $queueName, string $jobId): array
    {
        $this->assertAdmin($user);
        return ['removed' => true, 'queueName' => $queueName, 'jobId' => $jobId];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
