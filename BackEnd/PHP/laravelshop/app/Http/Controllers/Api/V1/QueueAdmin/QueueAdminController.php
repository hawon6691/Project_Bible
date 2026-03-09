<?php

namespace App\Http\Controllers\Api\V1\QueueAdmin;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\QueueAdmin\Services\QueueAdminService;
use Illuminate\Http\Request;

class QueueAdminController extends ApiController
{
    public function __construct(private readonly QueueAdminService $service) {}
    public function supported(Request $request) { return $this->success($this->service->supported($request->user())); }
    public function stats(Request $request) { return $this->success($this->service->stats($request->user())); }
    public function failed(Request $request, string $queueName) { return $this->success($this->service->failed($request->user(), $queueName, (int) $request->query('page', 1), (int) $request->query('limit', 20))); }
    public function retryFailed(Request $request, string $queueName) { return $this->success($this->service->retryFailed($request->user(), $queueName, (int) $request->query('limit', 10))); }
    public function autoRetry(Request $request) { return $this->success($this->service->autoRetry($request->user(), (int) $request->query('perQueueLimit', 10), (int) $request->query('maxTotal', 10))); }
    public function retryJob(Request $request, string $queueName, string $jobId) { return $this->success($this->service->retryJob($request->user(), $queueName, $jobId)); }
    public function removeJob(Request $request, string $queueName, string $jobId) { return $this->success($this->service->removeJob($request->user(), $queueName, $jobId)); }
}
