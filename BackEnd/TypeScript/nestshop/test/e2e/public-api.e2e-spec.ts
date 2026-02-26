import { INestApplication } from '@nestjs/common';
import { HealthController } from '../../src/health/health.controller';
import { HealthService } from '../../src/health/health.service';
import { ErrorCodeController } from '../../src/common/controllers/error-code.controller';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Public API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const healthServiceMock = {
    getHealth: jest.fn().mockResolvedValue({
      status: 'ok',
      timestamp: new Date().toISOString(),
      checks: {
        database: { status: 'up', latencyMs: 10, message: null },
        redis: { status: 'up', latencyMs: 3, message: null },
        elasticsearch: { status: 'up', latencyMs: 12, message: null },
      },
      summary: {
        upCount: 3,
        downCount: 0,
      },
    }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [HealthController, ErrorCodeController],
      providers: [{ provide: HealthService, useValue: healthServiceMock }],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /health should return wrapped success response', async () => {
    const res = await client.get('/health');
    expect(res.status).toBe(200);

    expect(res.body.success).toBe(true);
    expect(res.body.data.status).toBe('ok');
    expect(res.body.data.summary.upCount).toBe(3);
    expect(typeof res.body.requestId).toBe('string');
  });

  it('GET /errors/codes should return code catalog', async () => {
    const res = await client.get('/errors/codes');
    expect(res.status).toBe(200);

    expect(res.body.success).toBe(true);
    expect(res.body.data.total).toBeGreaterThan(0);
    expect(Array.isArray(res.body.data.items)).toBe(true);
    expect(res.body.data.items.some((item: { code: string }) => item.code === 'AUTH_001')).toBe(true);
  });
});
