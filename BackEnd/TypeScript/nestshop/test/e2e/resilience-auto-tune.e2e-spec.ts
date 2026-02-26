import { INestApplication } from '@nestjs/common';
import { ResilienceController } from '../../src/resilience/resilience.controller';
import { ResilienceService } from '../../src/resilience/resilience.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Resilience Auto Tune E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  let service: ResilienceService;

  beforeAll(async () => {
    process.env.RESILIENCE_AUTO_TUNE_ENABLED = 'true';
    process.env.RESILIENCE_AUTO_TUNE_MIN_SAMPLES = '5';
    process.env.RESILIENCE_AUTO_TUNE_COOLDOWN_MS = '0';

    app = await createE2eApp({
      controllers: [ResilienceController],
      providers: [ResilienceService],
    });
    client = await startTestServer(app);
    service = app.get(ResilienceService);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /resilience/circuit-breakers/policies should return tuned policy after repeated failures', async () => {
    for (let i = 0; i < 6; i += 1) {
      try {
        await service.execute(
          'payment-gateway',
          async () => {
            throw new Error('gateway failure');
          },
          {
            failureThreshold: 50,
            openTimeoutMs: 5_000,
            halfOpenSuccessThreshold: 2,
          },
        );
      } catch {
        // expected
      }
    }

    const res = await client.get('/resilience/circuit-breakers/policies');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data.items)).toBe(true);
    expect(res.body.data.items.length).toBeGreaterThan(0);
    expect(res.body.data.items[0].name).toBe('payment-gateway');
    expect(res.body.data.items[0].options.failureThreshold).toBeLessThanOrEqual(50);
  });
});
