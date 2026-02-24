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
    const [healthResult, searchSyncResult, crawlerResult, queueResult] = await Promise.allSettled([
      this.healthService.getHealth(),
      this.searchSyncService.getOutboxSummary(),
      this.crawlerService.getMonitoringSummary(),
      this.queueAdminService.getQueueStats(),
    ]);

    const errors: Record<string, string> = {};
    const health = this.unwrapOrNull('health', healthResult, errors);
    const searchSync = this.unwrapOrNull('searchSync', searchSyncResult, errors);
    const crawler = this.unwrapOrNull('crawler', crawlerResult, errors);
    const queue = this.unwrapOrNull('queue', queueResult, errors);

    return {
      checkedAt: new Date().toISOString(),
      overallStatus: Object.keys(errors).length ? 'degraded' : 'up',
      health,
      searchSync,
      crawler,
      queue,
      errors,
    };
  }

  private unwrapOrNull<T>(
    key: string,
    result: PromiseSettledResult<T>,
    errors: Record<string, string>,
  ): T | null {
    if (result.status === 'fulfilled') {
      return result.value;
    }

    errors[key] = result.reason instanceof Error ? result.reason.message : 'Unknown error';
    return null;
  }
}
