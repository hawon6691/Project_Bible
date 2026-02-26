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

describe('Ops Dashboard Dependency Failures E2E', () => {
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
            getHealth: jest.fn().mockRejectedValue(new Error('health unavailable')),
          },
        },
        {
          provide: SearchSyncService,
          useValue: {
            getOutboxSummary: jest.fn().mockRejectedValue(new Error('search unavailable')),
          },
        },
        {
          provide: CrawlerService,
          useValue: {
            getMonitoringSummary: jest.fn().mockRejectedValue(new Error('crawler unavailable')),
          },
        },
        {
          provide: QueueAdminService,
          useValue: {
            getQueueStats: jest.fn().mockRejectedValue(new Error('queue unavailable')),
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
    await app.close();
  });

  it('GET /admin/ops-dashboard/summary should return degraded and collect all errors', async () => {
    const res = await client.get('/admin/ops-dashboard/summary');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.overallStatus).toBe('degraded');
    expect(res.body.data.health).toBeNull();
    expect(res.body.data.searchSync).toBeNull();
    expect(res.body.data.crawler).toBeNull();
    expect(res.body.data.queue).toBeNull();
    expect(res.body.data.errors).toEqual(
      expect.objectContaining({
        health: 'health unavailable',
        searchSync: 'search unavailable',
        crawler: 'crawler unavailable',
        queue: 'queue unavailable',
      }),
    );
    expect(res.body.data.alerts.some((item: { key: string }) => item.key === 'partial_failure')).toBe(true);
  });
});
