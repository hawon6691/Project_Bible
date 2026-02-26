import { Module } from '@nestjs/common';
import { APP_INTERCEPTOR } from '@nestjs/core';
import { ConfigModule } from '@nestjs/config';
import { CrawlerModule } from '../crawler/crawler.module';
import { OpsDashboardModule } from '../ops-dashboard/ops-dashboard.module';
import { QueueAdminModule } from '../queue-admin/queue-admin.module';
import { ResilienceModule } from '../resilience/resilience.module';
import { SearchSyncModule } from '../search-sync/search-sync.module';
import { ObservabilityController } from './observability.controller';
import { ObservabilityTraceInterceptor } from './observability-trace.interceptor';
import { ObservabilityService } from './observability.service';

@Module({
  imports: [ConfigModule, QueueAdminModule, ResilienceModule, SearchSyncModule, CrawlerModule, OpsDashboardModule],
  controllers: [ObservabilityController],
  providers: [
    ObservabilityService,
    {
      provide: APP_INTERCEPTOR,
      useClass: ObservabilityTraceInterceptor,
    },
  ],
  exports: [ObservabilityService],
})
export class ObservabilityModule {}
