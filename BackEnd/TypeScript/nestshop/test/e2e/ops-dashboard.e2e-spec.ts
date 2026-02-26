import { INestApplication } from '@nestjs/common';
import { OpsDashboardController } from '../../src/ops-dashboard/ops-dashboard.controller';
import { OpsDashboardService } from '../../src/ops-dashboard/ops-dashboard.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Ops Dashboard E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const opsDashboardServiceMock = {
    getSummary: jest.fn().mockResolvedValue({
      checkedAt: new Date().toISOString(),
      overallStatus: 'up',
      health: {
        status: 'up',
        checks: {
          database: { status: 'up', latencyMs: 10 },
          redis: { status: 'up', latencyMs: 3 },
          elasticsearch: { status: 'up', latencyMs: 12 },
        },
      },
      searchSync: { pending: 2, processing: 1, completed: 50, failed: 0 },
      crawler: {
        totalRuns: 30,
        queuedRuns: 1,
        processingRuns: 1,
        successRuns: 27,
        failedRuns: 2,
        successRate: 93.1,
      },
      queue: {
        total: 4,
        items: [
          {
            queueName: 'video-transcode',
            paused: false,
            counts: { waiting: 3, active: 1, delayed: 0, completed: 100, failed: 2 },
          },
        ],
      },
      errors: {},
      alerts: [],
      alertCount: 0,
    }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [OpsDashboardController],
      providers: [{ provide: OpsDashboardService, useValue: opsDashboardServiceMock }],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /admin/ops-dashboard/summary should return aggregated operational summary', async () => {
    const res = await client.get('/admin/ops-dashboard/summary');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.health.status).toBe('up');
    expect(res.body.data.queue.total).toBe(4);
    expect(res.body.data.alertCount).toBe(0);
    expect(opsDashboardServiceMock.getSummary).toHaveBeenCalledTimes(1);
  });
});
