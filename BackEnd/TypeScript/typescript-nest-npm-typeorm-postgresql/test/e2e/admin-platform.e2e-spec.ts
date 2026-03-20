import { INestApplication } from '@nestjs/common';
import { CrawlerController } from '../../src/crawler/crawler.controller';
import { CrawlerService } from '../../src/crawler/crawler.service';
import { ResilienceController } from '../../src/resilience/resilience.controller';
import { ResilienceService } from '../../src/resilience/resilience.service';
import { SearchController } from '../../src/search/search.controller';
import { SearchService } from '../../src/search/search.service';
import { SearchSyncController } from '../../src/search-sync/search-sync.controller';
import { SearchSyncService } from '../../src/search-sync/search-sync.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Admin Platform E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const searchServiceMock = {
    getIndexStatus: jest.fn().mockResolvedValue({
      engine: 'elasticsearch',
      index: 'products_v1',
      documentCount: 1200,
      healthy: true,
    }),
    reindexAllProducts: jest.fn().mockResolvedValue({
      message: '전체 재색인 작업이 큐에 등록되었습니다.',
      queued: true,
    }),
    reindexProduct: jest.fn().mockResolvedValue({
      message: '단일 상품 재색인 작업이 큐에 등록되었습니다.',
      productId: 101,
    }),
  };

  const searchSyncServiceMock = {
    getOutboxSummary: jest.fn().mockResolvedValue({
      pending: 3,
      processing: 1,
      failed: 2,
      completed: 40,
    }),
    requeueFailed: jest.fn().mockImplementation(async (limit?: number) => ({
      requeuedCount: limit ?? 2,
    })),
  };

  const crawlerServiceMock = {
    getJobs: jest.fn().mockResolvedValue({
      items: [{ id: 1, name: '쿠팡 노트북 수집', isActive: true }],
      meta: { page: 1, limit: 20, totalItems: 1, totalPages: 1 },
    }),
    getMonitoringSummary: jest.fn().mockResolvedValue({
      queued: 2,
      processing: 1,
      successToday: 18,
      failedToday: 0,
    }),
  };

  const resilienceServiceMock = {
    getSnapshots: jest.fn().mockReturnValue([
      { name: 'search', state: 'CLOSED', failureCount: 0 },
      { name: 'payment', state: 'OPEN', failureCount: 5 },
    ]),
    getSnapshot: jest.fn().mockImplementation((name: string) => ({
      name,
      state: name === 'payment' ? 'OPEN' : 'CLOSED',
      failureCount: name === 'payment' ? 5 : 0,
    })),
    reset: jest.fn().mockImplementation((name: string) => ({
      message: 'Circuit Breaker가 초기화되었습니다.',
      name,
    })),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [SearchController, SearchSyncController, CrawlerController, ResilienceController],
      providers: [
        { provide: SearchService, useValue: searchServiceMock },
        { provide: SearchSyncService, useValue: searchSyncServiceMock },
        { provide: CrawlerService, useValue: crawlerServiceMock },
        { provide: ResilienceService, useValue: resilienceServiceMock },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /search/admin/index/status should return index health', async () => {
    const res = await client.get('/search/admin/index/status');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.engine).toBe('elasticsearch');
    expect(searchServiceMock.getIndexStatus).toHaveBeenCalledTimes(1);
  });

  it('POST /search/admin/index/reindex should queue reindex job', async () => {
    const res = await client.post('/search/admin/index/reindex', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.queued).toBe(true);
    expect(searchServiceMock.reindexAllProducts).toHaveBeenCalledTimes(1);
  });

  it('POST /search/admin/index/products/:id/reindex should queue single product job', async () => {
    const res = await client.post('/search/admin/index/products/101/reindex', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.productId).toBe(101);
    expect(searchServiceMock.reindexProduct).toHaveBeenCalledWith(101);
  });

  it('GET /search/admin/index/outbox/summary should return summary', async () => {
    const res = await client.get('/search/admin/index/outbox/summary');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.failed).toBe(2);
    expect(searchSyncServiceMock.getOutboxSummary).toHaveBeenCalledTimes(1);
  });

  it('POST /search/admin/index/outbox/requeue-failed should parse limit query', async () => {
    const res = await client.post('/search/admin/index/outbox/requeue-failed?limit=7', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.requeuedCount).toBe(7);
    expect(searchSyncServiceMock.requeueFailed).toHaveBeenCalledWith(7);
  });

  it('GET /crawler/admin/jobs should validate pagination query', async () => {
    const res = await client.get('/crawler/admin/jobs?limit=0');
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('COMMON_001');
  });

  it('GET /crawler/admin/monitoring should return monitoring summary', async () => {
    const res = await client.get('/crawler/admin/monitoring');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.processing).toBe(1);
    expect(crawlerServiceMock.getMonitoringSummary).toHaveBeenCalledTimes(1);
  });

  it('GET /resilience/circuit-breakers and detail should return snapshots', async () => {
    const list = await client.get('/resilience/circuit-breakers');
    expect(list.status).toBe(200);
    expect(list.body.success).toBe(true);
    expect(Array.isArray(list.body.data.items)).toBe(true);

    const detail = await client.get('/resilience/circuit-breakers/payment');
    expect(detail.status).toBe(200);
    expect(detail.body.success).toBe(true);
    expect(detail.body.data.name).toBe('payment');
    expect(resilienceServiceMock.getSnapshot).toHaveBeenCalledWith('payment');
  });

  it('POST /resilience/circuit-breakers/:name/reset should reset breaker', async () => {
    const res = await client.post('/resilience/circuit-breakers/payment/reset', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.name).toBe('payment');
    expect(resilienceServiceMock.reset).toHaveBeenCalledWith('payment');
  });
});
