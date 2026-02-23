import { InjectQueue } from '@nestjs/bull';
import { HttpStatus, Injectable } from '@nestjs/common';
import { Job, Queue } from 'bull';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { FailedJobsQueryDto } from './dto/failed-jobs-query.dto';
import { RetryFailedJobsDto } from './dto/retry-failed-jobs.dto';

const SUPPORTED_QUEUES = ['activity-log', 'video-transcode', 'crawler-collect', 'search-index-sync'] as const;
type SupportedQueueName = (typeof SUPPORTED_QUEUES)[number];

@Injectable()
export class QueueAdminService {
  constructor(
    @InjectQueue('activity-log')
    private readonly activityQueue: Queue,
    @InjectQueue('video-transcode')
    private readonly videoTranscodeQueue: Queue,
    @InjectQueue('crawler-collect')
    private readonly crawlerCollectQueue: Queue,
    @InjectQueue('search-index-sync')
    private readonly searchIndexSyncQueue: Queue,
  ) {}

  async getFailedJobs(queueName: string, query: FailedJobsQueryDto) {
    const queue = this.resolveQueue(queueName);
    const start = query.skip;
    const end = start + query.limit - 1;
    const jobs = await queue.getFailed(start, end);
    const totalFailed = await queue.getJobCountByTypes('failed');

    const items = jobs.map((job) => this.toJobSnapshot(job));
    if (query.newestFirst === false) {
      items.reverse();
    }

    return new PaginationResponseDto(items, totalFailed, query.page, query.limit);
  }

  async retryFailedJobs(queueName: string, dto: RetryFailedJobsDto) {
    const queue = this.resolveQueue(queueName);
    const target = await queue.getFailed(0, (dto.limit ?? 50) - 1);

    let requeuedCount = 0;
    const failedIds: string[] = [];
    for (const job of target) {
      try {
        await job.retry();
        requeuedCount += 1;
        failedIds.push(String(job.id));
      } catch {
        // 이미 active/wait 상태로 이동했거나 재시도 불가한 잡은 건너뛴다.
      }
    }

    return {
      queueName,
      requested: target.length,
      requeuedCount,
      jobIds: failedIds,
    };
  }

  async retryJob(queueName: string, jobId: string) {
    const queue = this.resolveQueue(queueName);
    const job = await queue.getJob(jobId);
    if (!job) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '해당 Job을 찾을 수 없습니다.');
    }

    await job.retry();
    return {
      queueName,
      jobId: String(job.id),
      retried: true,
    };
  }

  async removeJob(queueName: string, jobId: string) {
    const queue = this.resolveQueue(queueName);
    const job = await queue.getJob(jobId);
    if (!job) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '해당 Job을 찾을 수 없습니다.');
    }

    await job.remove();
    return {
      queueName,
      jobId: String(job.id),
      removed: true,
    };
  }

  getSupportedQueues() {
    return [...SUPPORTED_QUEUES];
  }

  private resolveQueue(queueName: string) {
    const map: Record<SupportedQueueName, Queue> = {
      'activity-log': this.activityQueue,
      'video-transcode': this.videoTranscodeQueue,
      'crawler-collect': this.crawlerCollectQueue,
      'search-index-sync': this.searchIndexSyncQueue,
    };

    if (!SUPPORTED_QUEUES.includes(queueName as SupportedQueueName)) {
      throw new BusinessException(
        'VALIDATION_FAILED',
        HttpStatus.BAD_REQUEST,
        `지원하지 않는 큐입니다. (${SUPPORTED_QUEUES.join(', ')})`,
      );
    }

    return map[queueName as SupportedQueueName];
  }

  private toJobSnapshot(job: Job) {
    return {
      id: String(job.id),
      name: job.name,
      data: job.data,
      timestamp: job.timestamp,
      processedOn: job.processedOn ?? null,
      finishedOn: job.finishedOn ?? null,
      attemptsMade: job.attemptsMade,
      failedReason: job.failedReason ?? null,
      stacktrace: Array.isArray(job.stacktrace) ? job.stacktrace.slice(-3) : [],
    };
  }
}
