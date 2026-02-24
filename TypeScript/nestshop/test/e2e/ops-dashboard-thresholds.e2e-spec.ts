import { INestApplication } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { CrawlerService } from '../../src/crawler/crawler.service';
import { HealthService } from '../../src/health/health.service';
import { OpsDashboardController } from '../../src/ops-dashboard/ops-dashboard.controller';
import { OpsDashboardService } from '../../src/ops-dashboard/ops-dashboard.service';
import { QueueAdminService } from '../../src/queue-admin/queue-admin.service';
import { SearchSyncService } from '../../src/search-sync/search-sync.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Ops Dashboard Thresholds E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const configMap: Record<string, string | undefined> = {};
  const configServiceMock = {
    get: jest.fn((key: string) => configMap[key]),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [OpsDashboardController],
      providers: [
        OpsDashboardService,
        {
          provide: HealthService,
          useValue: {
            getHealth: jest.fn().mockResolvedValue({
              status: 'up',
              checks: {
                database: { status: 'up' },
                redis: { status: 'up' },
                elasticsearch: { status: 'up' },
              },
            }),
          },
        },
        {
          provide: SearchSyncService,
          useValue: {
            getOutboxSummary: jest.fn().mockResolvedValue({
              pending: 0,
              processing: 0,
              completed: 10,
              failed: 1,
            }),
          },
        },
        {
          provide: CrawlerService,
          useValue: {
            getMonitoringSummary: jest.fn().mockResolvedValue({
              totalRuns: 10,
              queuedRuns: 0,
              processingRuns: 0,
              successRuns: 9,
              failedRuns: 1,
              successRate: 90,
            }),
          },
        },
        {
          provide: QueueAdminService,
          useValue: {
            getQueueStats: jest.fn().mockResolvedValue({
              total: 1,
              items: [
                {
                  queueName: 'video-transcode',
                  paused: false,
                  counts: { waiting: 0, active: 0, delayed: 0, completed: 20, failed: 1 },
                },
              ],
            }),
          },
        },
        { provide: ConfigService, useValue: configServiceMock },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('should suppress warning alerts when thresholds are higher than current failures', async () => {
    configMap.OPS_ALERT_SEARCH_FAILED_THRESHOLD = '2';
    configMap.OPS_ALERT_CRAWLER_FAILED_RUNS_THRESHOLD = '2';
    configMap.OPS_ALERT_QUEUE_FAILED_THRESHOLD = '2';

    const res = await client.get('/admin/ops-dashboard/summary');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.overallStatus).toBe('up');
    expect(res.body.data.alertCount).toBe(0);
  });

  it('should emit warning alerts when thresholds are met', async () => {
    configMap.OPS_ALERT_SEARCH_FAILED_THRESHOLD = '1';
    configMap.OPS_ALERT_CRAWLER_FAILED_RUNS_THRESHOLD = '1';
    configMap.OPS_ALERT_QUEUE_FAILED_THRESHOLD = '1';

    const res = await client.get('/admin/ops-dashboard/summary');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.alertCount).toBeGreaterThanOrEqual(3);
    const keys = res.body.data.alerts.map((item: { key: string }) => item.key);
    expect(keys).toEqual(expect.arrayContaining(['searchSync', 'crawler', 'queue']));
  });
});
