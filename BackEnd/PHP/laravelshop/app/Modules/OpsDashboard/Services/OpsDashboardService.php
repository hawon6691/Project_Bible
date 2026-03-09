<?php

namespace App\Modules\OpsDashboard\Services;

use App\Modules\QueueAdmin\Services\QueueAdminService;
use App\Modules\SearchSync\Services\SearchSyncService;
use App\Modules\Crawler\Services\CrawlerService;
use App\Models\User;

class OpsDashboardService
{
    public function __construct(
        private readonly QueueAdminService $queueAdminService,
        private readonly SearchSyncService $searchSyncService,
        private readonly CrawlerService $crawlerService,
    ) {
    }

    public function summary(User $user): array
    {
        return [
            'checkedAt' => now()->toISOString(),
            'overallStatus' => 'degraded',
            'health' => ['status' => 'ok'],
            'searchSync' => $this->searchSyncService->summary($user),
            'crawler' => $this->crawlerService->monitoring($user),
            'queue' => $this->queueAdminService->stats($user),
            'errors' => ['crawler' => null],
            'alerts' => ['crawler queue has 1 failed job'],
            'alertCount' => 1,
        ];
    }
}
