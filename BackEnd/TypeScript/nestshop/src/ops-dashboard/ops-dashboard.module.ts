import { Module } from '@nestjs/common';
import { CrawlerModule } from '../crawler/crawler.module';
import { HealthModule } from '../health/health.module';
import { QueueAdminModule } from '../queue-admin/queue-admin.module';
import { SearchSyncModule } from '../search-sync/search-sync.module';
import { OpsDashboardController } from './ops-dashboard.controller';
import { OpsDashboardService } from './ops-dashboard.service';

@Module({
  imports: [HealthModule, SearchSyncModule, CrawlerModule, QueueAdminModule],
  controllers: [OpsDashboardController],
  providers: [OpsDashboardService],
  exports: [OpsDashboardService],
})
export class OpsDashboardModule {}
