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

describe('Ops Dashboard Resilience E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [OpsDashboardController],
      providers: [
        OpsDashboardService,
        {
          provide: HealthService,
          useValue: {
            getHealth: jest.fn().mockResolvedValue({ status: 'up' }),
          },
        },
        {
          provide: SearchSyncService,
          useValue: {
            getOutboxSummary: jest.fn().mockRejectedValue(new Error('search sync unavailable')),
          },
        },
        {
          provide: CrawlerService,
          useValue: {
            getMonitoringSummary: jest.fn().mockResolvedValue({ successRuns: 10, failedRuns: 0 }),
          },
        },
        {
          provide: QueueAdminService,
          useValue: {
            getQueueStats: jest.fn().mockResolvedValue({ total: 4, items: [] }),
          },
        },
        {
          provide: ConfigService,
          useValue: {
            get: jest.fn().mockReturnValue(undefined),
          },
        },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    if (app) {
      await app.close();
    }
  });

  it('GET /admin/ops-dashboard/summary should return degraded summary on partial failures', async () => {
    const res = await client.get('/admin/ops-dashboard/summary');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.overallStatus).toBe('degraded');
    expect(res.body.data.searchSync).toBeNull();
    expect(res.body.data.errors.searchSync).toBe('search sync unavailable');
    expect(res.body.data.alertCount).toBeGreaterThan(0);
    expect(Array.isArray(res.body.data.alerts)).toBe(true);
    expect(res.body.data.alerts.some((item: { key: string }) => item.key === 'partial_failure')).toBe(true);
  });
});
