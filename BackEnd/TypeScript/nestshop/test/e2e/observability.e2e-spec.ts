import { INestApplication } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { CrawlerService } from '../../src/crawler/crawler.service';
import { OpsDashboardService } from '../../src/ops-dashboard/ops-dashboard.service';
import { ObservabilityController } from '../../src/observability/observability.controller';
import { ObservabilityService } from '../../src/observability/observability.service';
import { QueueAdminService } from '../../src/queue-admin/queue-admin.service';
import { ResilienceService } from '../../src/resilience/resilience.service';
import { SearchSyncService } from '../../src/search-sync/search-sync.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Observability E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  let service: ObservabilityService;

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [ObservabilityController],
      providers: [
        ObservabilityService,
        {
          provide: ConfigService,
          useValue: { get: jest.fn().mockReturnValue(undefined) },
        },
        {
          provide: QueueAdminService,
          useValue: { getQueueStats: jest.fn().mockResolvedValue({ total: 1, items: [] }) },
        },
        {
          provide: ResilienceService,
          useValue: {
            getSnapshots: jest.fn().mockReturnValue([]),
            getAdaptivePolicies: jest.fn().mockReturnValue([]),
          },
        },
        {
          provide: SearchSyncService,
          useValue: { getOutboxSummary: jest.fn().mockResolvedValue({ total: 0, failed: 0 }) },
        },
        {
          provide: CrawlerService,
          useValue: { getMonitoringSummary: jest.fn().mockResolvedValue({ successRuns: 10, failedRuns: 0 }) },
        },
        {
          provide: OpsDashboardService,
          useValue: { getSummary: jest.fn().mockResolvedValue({ overallStatus: 'up', alertCount: 0 }) },
        },
      ],
    });

    client = await startTestServer(app);
    service = app.get(ObservabilityService);
    service.recordTrace({
      requestId: 'trace-1',
      method: 'GET',
      path: '/health',
      statusCode: 200,
      durationMs: 14,
      ip: '127.0.0.1',
      userId: null,
      timestamp: new Date().toISOString(),
    });
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /admin/observability/metrics should return latency and error summary', async () => {
    const res = await client.get('/admin/observability/metrics');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.totalRequests).toBeGreaterThan(0);
    expect(typeof res.body.data.p95LatencyMs).toBe('number');
  });

  it('GET /admin/observability/traces should return recent trace items', async () => {
    const res = await client.get('/admin/observability/traces?limit=10&pathContains=health');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data.items)).toBe(true);
    expect(res.body.data.items[0].requestId).toBe('trace-1');
  });

  it('GET /admin/observability/dashboard should return integrated dashboard payload', async () => {
    const res = await client.get('/admin/observability/dashboard');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.metrics).toBeDefined();
    expect(res.body.data.queue).toBeDefined();
    expect(res.body.data.resilience).toBeDefined();
  });
});
