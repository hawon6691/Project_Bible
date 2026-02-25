import { INestApplication } from '@nestjs/common';
import { HttpStatus } from '@nestjs/common';
import { BusinessException } from '../../src/common/exceptions/business.exception';
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
    getQueueStats: jest.fn().mockResolvedValue({
      total: 4,
      items: [
        {
          queueName: 'video-transcode',
          paused: false,
          counts: { waiting: 2, active: 1, delayed: 0, completed: 100, failed: 3 },
        },
      ],
    }),
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
    autoRetryFailed: jest.fn().mockResolvedValue({
      perQueueLimit: 20,
      maxTotal: 100,
      retriedTotal: 3,
      items: [{ queueName: 'video-transcode', candidateCount: 3, retriedCount: 3, jobIds: ['1001', '1002', '1003'] }],
    }),
    retryJob: jest.fn().mockImplementation(async (_queueName: string, jobId: string) => {
      if (jobId === 'completed-job') {
        throw new BusinessException(
          'VALIDATION_FAILED',
          HttpStatus.BAD_REQUEST,
          '실패 상태 Job만 재시도할 수 있습니다. (current: completed)',
        );
      }

      return {
        queueName: 'video-transcode',
        jobId,
        retried: true,
      };
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

  it('GET /admin/queues/stats should return queue counts', async () => {
    const res = await client.get('/admin/queues/stats');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.total).toBe(4);
    expect(Array.isArray(res.body.data.items)).toBe(true);
    expect(res.body.data.items[0].queueName).toBe('video-transcode');
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

    await client.get('/admin/queues/video-transcode/failed?page=1&limit=20&newestFirst=false');
    expect(queueAdminServiceMock.getFailedJobs).toHaveBeenLastCalledWith(
      'video-transcode',
      expect.objectContaining({ newestFirst: 'false' }),
    );
  });

  it('POST /admin/queues/:queueName/failed/retry should retry failed jobs', async () => {
    const res = await client.post('/admin/queues/video-transcode/failed/retry?limit=2', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.requeuedCount).toBe(2);
  });

  it('POST /admin/queues/auto-retry should run auto retry policy', async () => {
    const res = await client.post('/admin/queues/auto-retry?perQueueLimit=20&maxTotal=100', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.retriedTotal).toBe(3);
    expect(queueAdminServiceMock.autoRetryFailed).toHaveBeenCalled();
  });

  it('POST /admin/queues/:queueName/jobs/:jobId/retry should retry single job', async () => {
    const res = await client.post('/admin/queues/video-transcode/jobs/1001/retry', {});
    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.retried).toBe(true);
  });

  it('POST /admin/queues/:queueName/jobs/:jobId/retry should reject non-failed job', async () => {
    const res = await client.post('/admin/queues/video-transcode/jobs/completed-job/retry', {});
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('COMMON_001');
  });

  it('DELETE /admin/queues/:queueName/jobs/:jobId should remove job', async () => {
    const res = await client.delete('/admin/queues/video-transcode/jobs/job-1001-a');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.removed).toBe(true);
    expect(queueAdminServiceMock.removeJob).toHaveBeenLastCalledWith('video-transcode', 'job-1001-a');
  });
});
