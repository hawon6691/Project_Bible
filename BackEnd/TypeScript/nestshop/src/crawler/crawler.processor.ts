import { Process, Processor } from '@nestjs/bull';
import { Job } from 'bull';
import { CrawlerCollectJobData, CrawlerService } from './crawler.service';

@Processor('crawler-collect')
export class CrawlerProcessor {
  constructor(private readonly crawlerService: CrawlerService) {}

  // 큐에 적재된 실행 건을 순차 처리하여 가격/스펙 수집 파이프라인을 수행한다.
  @Process('collect')
  async processCollect(job: Job<CrawlerCollectJobData>) {
    await this.crawlerService.processRun(job.data.runId);
  }
}
