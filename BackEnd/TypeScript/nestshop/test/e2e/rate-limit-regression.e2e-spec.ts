import { Controller, Get, INestApplication, Post, UseGuards } from '@nestjs/common';
import { Public } from '../../src/common/decorators/public.decorator';
import { RateLimitGuard } from '../../src/common/guards/rate-limit.guard';
import { createE2eApp } from './test-app.factory';

@Controller('rate-limit-test')
@UseGuards(RateLimitGuard)
class RateLimitTestController {
  @Public()
  @Get('ping')
  ping() {
    return { ok: true };
  }

  @Public()
  @Post('auth/login')
  login() {
    return { ok: true };
  }
}

describe('Rate Limit Regression E2E', () => {
  let app: INestApplication;
  let baseUrl: string;

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [RateLimitTestController],
      providers: [RateLimitGuard],
    });

    const guard = app.get(RateLimitGuard) as any;
    guard.defaultLimitPerWindow = 3;
    guard.authLimitPerWindow = 2;
    guard.windowMs = 60_000;

    await app.listen(0);
    const address = app.getHttpServer().address() as { port: number };
    baseUrl = `http://127.0.0.1:${address.port}`;
  });

  afterAll(async () => {
    await app.close();
  });

  it('should return 429 after exceeding default path limit', async () => {
    await fetch(`${baseUrl}/rate-limit-test/ping`);
    await fetch(`${baseUrl}/rate-limit-test/ping`);
    await fetch(`${baseUrl}/rate-limit-test/ping`);
    const blocked = await fetch(`${baseUrl}/rate-limit-test/ping`);
    const body = await blocked.json();

    expect(blocked.status).toBe(429);
    expect(body.success).toBe(false);
    expect(body.errorCode).toBe('COMMON_004');
  });

  it('should apply stricter limit for /auth path', async () => {
    await fetch(`${baseUrl}/rate-limit-test/auth/login`, { method: 'POST' });
    await fetch(`${baseUrl}/rate-limit-test/auth/login`, { method: 'POST' });
    const blocked = await fetch(`${baseUrl}/rate-limit-test/auth/login`, { method: 'POST' });
    const body = await blocked.json();

    expect(blocked.status).toBe(429);
    expect(body.success).toBe(false);
    expect(body.errorCode).toBe('COMMON_004');
  });
});
