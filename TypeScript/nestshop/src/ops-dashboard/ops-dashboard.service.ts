import { Injectable } from '@nestjs/common';
import { CrawlerService } from '../crawler/crawler.service';
import { HealthService } from '../health/health.service';
import { QueueAdminService } from '../queue-admin/queue-admin.service';
import { SearchSyncService } from '../search-sync/search-sync.service';

@Injectable()
export class OpsDashboardService {
  constructor(
    private readonly healthService: HealthService,
    private readonly searchSyncService: SearchSyncService,
    private readonly crawlerService: CrawlerService,
    private readonly queueAdminService: QueueAdminService,
  ) {}

  // 운영자가 빠르게 상태를 판단할 수 있도록 핵심 운영 지표를 단일 응답으로 묶는다.
  async getSummary() {
    const [health, searchSync, crawler, queue] = await Promise.all([
      this.healthService.getHealth(),
      this.searchSyncService.getOutboxSummary(),
      this.crawlerService.getMonitoringSummary(),
      this.queueAdminService.getQueueStats(),
    ]);

    return {
      checkedAt: new Date().toISOString(),
      health,
      searchSync,
      crawler,
      queue,
    };
  }
}
