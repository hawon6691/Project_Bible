import { Process, Processor } from '@nestjs/bull';
import { Job } from 'bull';
import { ActivityService } from './activity.service';

interface RecentProductJobData {
  userId: number;
  productId: number;
}

interface SearchHistoryJobData {
  userId: number;
  keyword: string;
}

@Processor('activity-log')
export class ActivityProcessor {
  constructor(private readonly activityService: ActivityService) {}

  @Process('recent-product')
  async handleRecentProduct(job: Job<RecentProductJobData>) {
    await this.activityService.persistRecentProduct(job.data.userId, job.data.productId);
  }

  @Process('search-history')
  async handleSearchHistory(job: Job<SearchHistoryJobData>) {
    await this.activityService.persistSearchHistory(job.data.userId, job.data.keyword);
  }
}
