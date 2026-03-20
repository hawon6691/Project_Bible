import { INestApplication } from '@nestjs/common';
import { HealthController } from '../../src/health/health.controller';
import { HealthService } from '../../src/health/health.service';
import { ErrorCodeController } from '../../src/common/controllers/error-code.controller';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

function expectSuccessEnvelope(body: any) {
  expect(body).toEqual(
    expect.objectContaining({
      success: true,
      data: expect.anything(),
      requestId: expect.any(String),
      timestamp: expect.any(String),
    }),
  );
}

describe('Public API Contract E2E', () => {
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

  it('GET /health contract should be stable', async () => {
    const res = await client.get('/health');
    expect(res.status).toBe(200);
    expectSuccessEnvelope(res.body);
    expect(res.body.data).toEqual(
      expect.objectContaining({
        status: expect.stringMatching(/^(ok|degraded)$/),
        checks: expect.objectContaining({
          database: expect.any(Object),
          redis: expect.any(Object),
          elasticsearch: expect.any(Object),
        }),
        summary: expect.objectContaining({
          upCount: expect.any(Number),
          downCount: expect.any(Number),
        }),
      }),
    );
  });

  it('GET /errors/codes contract should be stable', async () => {
    const res = await client.get('/errors/codes');
    expect(res.status).toBe(200);
    expectSuccessEnvelope(res.body);
    expect(res.body.data).toEqual(
      expect.objectContaining({
        total: expect.any(Number),
        items: expect.any(Array),
      }),
    );
    expect(res.body.data.items.length).toBeGreaterThan(0);
    expect(res.body.data.items[0]).toEqual(
      expect.objectContaining({
        code: expect.any(String),
        key: expect.any(String),
        message: expect.any(String),
      }),
    );
  });
});
