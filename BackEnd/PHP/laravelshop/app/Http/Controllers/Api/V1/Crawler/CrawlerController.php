<?php

namespace App\Http\Controllers\Api\V1\Crawler;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Crawler\Requests\StoreCrawlerJobRequest;
use App\Modules\Crawler\Requests\TriggerCrawlerRequest;
use App\Modules\Crawler\Requests\UpdateCrawlerJobRequest;
use App\Modules\Crawler\Services\CrawlerService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Crawler')]
class CrawlerController extends ApiController
{
    public function __construct(private readonly CrawlerService $service) {}

    public function jobs(Request $request)
    {
        return $this->success($this->service->jobs($request->user(), $request->query('status'), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function storeJob(StoreCrawlerJobRequest $request)
    {
        return $this->success($this->service->createJob($request->user(), $request->validated()), [], 201);
    }

    public function updateJob(UpdateCrawlerJobRequest $request, int $id)
    {
        return $this->success($this->service->updateJob($request->user(), $id, $request->validated()));
    }

    public function destroyJob(Request $request, int $id)
    {
        return $this->success($this->service->deleteJob($request->user(), $id));
    }

    public function runJob(Request $request, int $id)
    {
        return $this->success($this->service->runJob($request->user(), $id));
    }

    public function trigger(TriggerCrawlerRequest $request)
    {
        return $this->success($this->service->trigger($request->user(), $request->validated()));
    }

    public function runs(Request $request)
    {
        return $this->success($this->service->runs($request->user(), $request->query('status'), $request->query('jobId') ? (int) $request->query('jobId') : null, (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function monitoring(Request $request)
    {
        return $this->success($this->service->monitoring($request->user()));
    }
}
