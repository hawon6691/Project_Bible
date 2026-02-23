import { Controller, Delete, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { FailedJobsQueryDto } from './dto/failed-jobs-query.dto';
import { RetryFailedJobsDto } from './dto/retry-failed-jobs.dto';
import { QueueAdminService } from './queue-admin.service';

@ApiTags('QueueAdmin')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller(API_ROUTES.QUEUE_ADMIN.BASE)
export class QueueAdminController {
  constructor(private readonly queueAdminService: QueueAdminService) {}

  @Get('supported')
  @ApiOperation({ summary: '관리 대상 큐 목록 조회 (Admin)' })
  getSupportedQueues() {
    return { items: this.queueAdminService.getSupportedQueues() };
  }

  @Get(API_ROUTES.QUEUE_ADMIN.FAILED)
  @ApiOperation({ summary: '큐 실패 Job 목록 조회 (Admin)' })
  getFailedJobs(@Param('queueName') queueName: string, @Query() query: FailedJobsQueryDto) {
    return this.queueAdminService.getFailedJobs(queueName, query);
  }

  @Post(API_ROUTES.QUEUE_ADMIN.FAILED_RETRY)
  @ApiOperation({ summary: '큐 실패 Job 일괄 재시도 (Admin)' })
  retryFailedJobs(@Param('queueName') queueName: string, @Query() dto: RetryFailedJobsDto) {
    return this.queueAdminService.retryFailedJobs(queueName, dto);
  }

  @Post(API_ROUTES.QUEUE_ADMIN.JOB_RETRY)
  @ApiOperation({ summary: '큐 실패 Job 개별 재시도 (Admin)' })
  retryJob(@Param('queueName') queueName: string, @Param('jobId', ParseIntPipe) jobId: number) {
    return this.queueAdminService.retryJob(queueName, String(jobId));
  }

  @Delete(API_ROUTES.QUEUE_ADMIN.JOB_DETAIL)
  @ApiOperation({ summary: '큐 Job 개별 삭제 (Admin)' })
  removeJob(@Param('queueName') queueName: string, @Param('jobId', ParseIntPipe) jobId: number) {
    return this.queueAdminService.removeJob(queueName, String(jobId));
  }
}
