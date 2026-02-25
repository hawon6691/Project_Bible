import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { CrawlerService } from '../crawler/crawler.service';
import { OpsDashboardService } from '../ops-dashboard/ops-dashboard.service';
import { QueueAdminService } from '../queue-admin/queue-admin.service';
import { ResilienceService } from '../resilience/resilience.service';
import { SearchSyncService } from '../search-sync/search-sync.service';
import { HttpTrace, MetricsSummary } from './observability.types';

@Injectable()
export class ObservabilityService {
  private readonly traces: HttpTrace[] = [];
  private readonly maxTraceBuffer: number;

  constructor(
    private readonly configService: ConfigService,
    private readonly queueAdminService: QueueAdminService,
    private readonly resilienceService: ResilienceService,
    private readonly searchSyncService: SearchSyncService,
    private readonly crawlerService: CrawlerService,
    private readonly opsDashboardService: OpsDashboardService,
  ) {
    this.maxTraceBuffer = this.readInt('OBS_TRACE_BUFFER_LIMIT', 500, 100, 5000);
  }

  // 최근 요청 추적 로그를 메모리에 순환 버퍼로 보관해 운영 대시보드 조회에 사용한다.
  recordTrace(trace: HttpTrace) {
    this.traces.push(trace);
    if (this.traces.length > this.maxTraceBuffer) {
      this.traces.splice(0, this.traces.length - this.maxTraceBuffer);
    }
  }

  getTraces(limit = 50, pathContains?: string) {
    const normalized = pathContains?.trim().toLowerCase();
    const filtered = normalized
      ? this.traces.filter((item) => item.path.toLowerCase().includes(normalized))
      : this.traces;

    return filtered.slice(-Math.max(1, limit)).reverse();
  }

  getMetricsSummary(windowMinutes = 15): MetricsSummary {
    const now = Date.now();
    const from = now - windowMinutes * 60_000;
    const windowed = this.traces.filter((item) => new Date(item.timestamp).getTime() >= from);

    const total = windowed.length;
    const errorCount = windowed.filter((item) => item.statusCode >= 400).length;
    const durations = windowed.map((item) => item.durationMs).sort((a, b) => a - b);

    const statusBuckets = {
      s2xx: windowed.filter((item) => item.statusCode >= 200 && item.statusCode < 300).length,
      s3xx: windowed.filter((item) => item.statusCode >= 300 && item.statusCode < 400).length,
      s4xx: windowed.filter((item) => item.statusCode >= 400 && item.statusCode < 500).length,
      s5xx: windowed.filter((item) => item.statusCode >= 500).length,
    };

    return {
      totalRequests: total,
      errorRequests: errorCount,
      errorRate: total > 0 ? Number((errorCount / total).toFixed(4)) : 0,
      avgLatencyMs: total > 0 ? Number((durations.reduce((acc, cur) => acc + cur, 0) / total).toFixed(2)) : 0,
      p95LatencyMs: this.percentile(durations, 0.95),
      p99LatencyMs: this.percentile(durations, 0.99),
      statusBuckets,
    };
  }

  async getDashboard() {
    const [queueStats, outboxSummary, crawlerSummary, opsSummary] = await Promise.all([
      this.queueAdminService.getQueueStats(),
      this.searchSyncService.getOutboxSummary(),
      this.crawlerService.getMonitoringSummary(),
      this.opsDashboardService.getSummary(),
    ]);

    return {
      checkedAt: new Date().toISOString(),
      process: {
        uptimeSec: Number(process.uptime().toFixed(2)),
        memory: process.memoryUsage(),
      },
      metrics: this.getMetricsSummary(),
      queue: queueStats,
      resilience: {
        circuits: this.resilienceService.getSnapshots(),
        adaptivePolicies: this.resilienceService.getAdaptivePolicies(),
      },
      searchSync: outboxSummary,
      crawler: crawlerSummary,
      opsSummary,
    };
  }

  private percentile(sorted: number[], p: number) {
    if (!sorted.length) {
      return 0;
    }
    const idx = Math.min(sorted.length - 1, Math.ceil(sorted.length * p) - 1);
    return sorted[idx];
  }

  private readInt(key: string, fallback: number, min: number, max: number) {
    const raw = this.configService.get<string>(key);
    const parsed = Number(raw);
    if (!Number.isFinite(parsed)) {
      return fallback;
    }
    return Math.max(min, Math.min(max, Math.trunc(parsed)));
  }
}
