import { Process, Processor } from '@nestjs/bull';
import { Job } from 'bull';
import { SearchSyncJobData, SearchSyncService } from './search-sync.service';

@Processor('search-index-sync')
export class SearchSyncProcessor {
  constructor(private readonly searchSyncService: SearchSyncService) {}

  // Outbox 이벤트를 소비해 검색 인덱스를 비동기로 동기화한다.
  @Process('sync')
  async handleSync(job: Job<SearchSyncJobData>) {
    await this.searchSyncService.processOutbox(job.data.outboxId);
  }
}
