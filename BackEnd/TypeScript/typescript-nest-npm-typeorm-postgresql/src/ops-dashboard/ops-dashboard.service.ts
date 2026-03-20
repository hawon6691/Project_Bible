import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
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
    private readonly configService: ConfigService,
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
    const alerts = this.buildAlerts({ health, searchSync, crawler, queue, errors });

    return {
      checkedAt: new Date().toISOString(),
      overallStatus: Object.keys(errors).length ? 'degraded' : 'up',
      health,
      searchSync,
      crawler,
      queue,
      errors,
      alerts,
      alertCount: alerts.length,
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

  private buildAlerts(params: {
    health: any;
    searchSync: any;
    crawler: any;
    queue: any;
    errors: Record<string, string>;
  }) {
    const alerts: Array<{ key: string; severity: 'warning' | 'critical'; message: string }> = [];

    const searchFailedThreshold = this.readInt('OPS_ALERT_SEARCH_FAILED_THRESHOLD', 1, 0);
    const crawlerFailedRunsThreshold = this.readInt('OPS_ALERT_CRAWLER_FAILED_RUNS_THRESHOLD', 1, 0);
    const queueFailedThreshold = this.readInt('OPS_ALERT_QUEUE_FAILED_THRESHOLD', 1, 0);

    if (params.health?.status && params.health.status !== 'up') {
      alerts.push({
        key: 'health',
        severity: 'critical',
        message: `헬스 상태가 비정상입니다. (status: ${params.health.status})`,
      });
    }

    if ((params.searchSync?.failed ?? 0) >= searchFailedThreshold && searchFailedThreshold > 0) {
      alerts.push({
        key: 'searchSync',
        severity: 'warning',
        message: `검색 동기화 실패 건이 임계치를 초과했습니다. (failed: ${params.searchSync.failed}, threshold: ${searchFailedThreshold})`,
      });
    }

    if ((params.crawler?.failedRuns ?? 0) >= crawlerFailedRunsThreshold && crawlerFailedRunsThreshold > 0) {
      alerts.push({
        key: 'crawler',
        severity: 'warning',
        message: `크롤러 실패 실행 건이 임계치를 초과했습니다. (failedRuns: ${params.crawler.failedRuns}, threshold: ${crawlerFailedRunsThreshold})`,
      });
    }

    const queueItems = params.queue?.items ?? [];
    const failedQueue = queueItems.find((item: any) => (item?.counts?.failed ?? 0) >= queueFailedThreshold);
    if (failedQueue && queueFailedThreshold > 0) {
      alerts.push({
        key: 'queue',
        severity: 'warning',
        message: `실패 Job이 있는 큐가 임계치를 초과했습니다. (${failedQueue.queueName}: ${failedQueue.counts.failed}, threshold: ${queueFailedThreshold})`,
      });
    }

    if (Object.keys(params.errors).length > 0) {
      alerts.push({
        key: 'partial_failure',
        severity: 'critical',
        message: `일부 지표 수집에 실패했습니다. (${Object.keys(params.errors).join(', ')})`,
      });
    }

    return alerts;
  }

  private readInt(key: string, fallback: number, min = Number.MIN_SAFE_INTEGER) {
    const raw = this.configService.get<string>(key);
    const parsed = Number(raw);
    if (!Number.isFinite(parsed)) {
      return fallback;
    }
    return Math.max(min, Math.trunc(parsed));
  }
}
