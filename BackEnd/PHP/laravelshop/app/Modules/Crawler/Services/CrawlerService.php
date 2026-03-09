<?php

namespace App\Modules\Crawler\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\CrawlerJob;
use App\Models\CrawlerRun;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class CrawlerService
{
    public function jobs(User $user, ?string $status, int $page, int $limit): array
    {
        $this->assertAdmin($user);
        $result = CrawlerJob::query()->when($status, fn ($q) => $q->where('status', $status))->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return ['items' => $result->items(), 'pagination' => ['page' => $result->currentPage(), 'limit' => $result->perPage(), 'total' => $result->total(), 'totalPages' => $result->lastPage()]];
    }

    public function createJob(User $user, array $payload): array
    {
        $this->assertAdmin($user);
        $job = CrawlerJob::query()->create(['name' => $payload['name'], 'job_type' => $payload['jobType'], 'status' => $payload['status'] ?? 'ACTIVE', 'payload' => $payload['payload'] ?? null]);

        return $job->toArray();
    }

    public function updateJob(User $user, int $id, array $payload): array
    {
        $this->assertAdmin($user);
        $job = CrawlerJob::query()->find($id);
        if (! $job) {
            throw new BusinessException('크롤러 작업을 찾을 수 없습니다.', 'CRAWLER_JOB_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        $job->forceFill(['name' => $payload['name'] ?? $job->name, 'job_type' => $payload['jobType'] ?? $job->job_type, 'status' => $payload['status'] ?? $job->status, 'payload' => array_key_exists('payload', $payload) ? $payload['payload'] : $job->payload])->save();

        return $job->toArray();
    }

    public function deleteJob(User $user, int $id): array
    {
        $this->assertAdmin($user);
        CrawlerJob::query()->where('id', $id)->delete();

        return ['message' => '크롤러 작업이 삭제되었습니다.'];
    }

    public function runJob(User $user, int $id): array
    {
        $this->assertAdmin($user);
        $run = CrawlerRun::query()->create(['crawler_job_id' => $id, 'status' => 'QUEUED', 'trigger_type' => 'MANUAL']);

        return ['message' => '크롤러 작업이 실행되었습니다.', 'runId' => $run->id];
    }

    public function trigger(User $user, array $payload): array
    {
        $this->assertAdmin($user);
        $run = CrawlerRun::query()->create(['crawler_job_id' => $payload['jobId'] ?? null, 'status' => 'QUEUED', 'trigger_type' => $payload['targetType'] ?? 'MANUAL']);

        return ['message' => '크롤러 트리거가 등록되었습니다.', 'runId' => $run->id];
    }

    public function runs(User $user, ?string $status, ?int $jobId, int $page, int $limit): array
    {
        $this->assertAdmin($user);
        $result = CrawlerRun::query()->when($status, fn ($q) => $q->where('status', $status))->when($jobId, fn ($q) => $q->where('crawler_job_id', $jobId))->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return ['items' => $result->items(), 'pagination' => ['page' => $result->currentPage(), 'limit' => $result->perPage(), 'total' => $result->total(), 'totalPages' => $result->lastPage()]];
    }

    public function monitoring(User $user): array
    {
        $this->assertAdmin($user);

        return ['jobCount' => CrawlerJob::query()->count(), 'queuedRunCount' => CrawlerRun::query()->where('status', 'QUEUED')->count(), 'latestRunId' => CrawlerRun::query()->max('id')];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
