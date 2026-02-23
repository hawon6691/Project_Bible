import { INestApplication } from '@nestjs/common';
import { QueueAdminController } from '../../src/queue-admin/queue-admin.controller';
import { QueueAdminService } from '../../src/queue-admin/queue-admin.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Queue Admin E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const queueAdminServiceMock = {
    getSupportedQueues: jest
      .fn()
      .mockReturnValue(['activity-log', 'video-transcode', 'crawler-collect', 'search-index-sync']),
    getFailedJobs: jest.fn().mockResolvedValue({
      items: [{ id: '1001', name: 'transcode', failedReason: 'ffmpeg error' }],
      meta: { page: 1, limit: 20, totalItems: 1, totalPages: 1 },
    }),
    retryFailedJobs: jest.fn().mockResolvedValue({
      queueName: 'video-transcode',
      requested: 2,
      requeuedCount: 2,
      jobIds: ['1001', '1002'],
    }),
    retryJob: jest.fn().mockResolvedValue({
      queueName: 'video-transcode',
      jobId: '1001',
      retried: true,
    }),
    removeJob: jest.fn().mockResolvedValue({
      queueName: 'video-transcode',
      jobId: '1001',
      removed: true,
    }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [QueueAdminController],
      providers: [{ provide: QueueAdminService, useValue: queueAdminServiceMock }],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /admin/queues/supported should return queue names', async () => {
    const res = await client.get('/admin/queues/supported');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data.items)).toBe(true);
  });

  it('GET /admin/queues/:queueName/failed should validate pagination', async () => {
    const invalid = await client.get('/admin/queues/video-transcode/failed?limit=0');
    expect(invalid.status).toBe(400);
    expect(invalid.body.success).toBe(false);
    expect(invalid.body.error.code).toBe('COMMON_001');

    const valid = await client.get('/admin/queues/video-transcode/failed?page=1&limit=20');
    expect(valid.status).toBe(200);
    expect(valid.body.success).toBe(true);
    expect(valid.body.meta.totalCount).toBe(1);
  });

  it('POST /admin/queues/:queueName/failed/retry should retry failed jobs', async () => {
    const res = await client.post('/admin/queues/video-transcode/failed/retry?limit=2', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.requeuedCount).toBe(2);
  });

  it('POST /admin/queues/:queueName/jobs/:jobId/retry should retry single job', async () => {
    const res = await client.post('/admin/queues/video-transcode/jobs/1001/retry', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.retried).toBe(true);
  });

  it('DELETE /admin/queues/:queueName/jobs/:jobId should remove job', async () => {
    const res = await client.delete('/admin/queues/video-transcode/jobs/1001');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.removed).toBe(true);
  });
});
