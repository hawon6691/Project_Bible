import { INestApplication } from '@nestjs/common';
import { HealthController } from '../../src/health/health.controller';
import { HealthService } from '../../src/health/health.service';
import { SearchController } from '../../src/search/search.controller';
import { SearchService } from '../../src/search/search.service';
import { createE2eApp } from '../e2e/test-app.factory';

async function bootstrap() {
  // CI 성능 스모크는 인프라 의존성을 제거하기 위해 목 서버를 사용한다.
  const healthServiceMock = {
    getHealth: async () => ({
      status: 'up',
      checkedAt: new Date().toISOString(),
      checks: {
        database: { status: 'up', latencyMs: 4 },
        redis: { status: 'up', latencyMs: 3 },
        elasticsearch: { status: 'up', latencyMs: 5 },
      },
    }),
  };

  const searchServiceMock = {
    search: async () => ({
      items: [{ id: 1, name: 'Perf Mock Product', lowestPrice: 10000 }],
      meta: {
        page: 1,
        limit: 20,
        totalItems: 1,
        totalPages: 1,
      },
      relatedKeywords: ['cpu'],
      engine: 'mock',
    }),
  };

  const app = (await createE2eApp({
    controllers: [HealthController, SearchController],
    providers: [
      { provide: HealthService, useValue: healthServiceMock },
      { provide: SearchService, useValue: searchServiceMock },
    ],
  })) as INestApplication;

  const port = Number(process.env.PERF_PORT ?? 3310);
  await app.listen(port);
  process.stdout.write(`perf-mock-server listening on ${port}\n`);

  const shutdown = async () => {
    await app.close();
    process.exit(0);
  };

  process.on('SIGINT', shutdown);
  process.on('SIGTERM', shutdown);
}

void bootstrap();
