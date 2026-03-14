import { BullModule } from '@nestjs/bull';
import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SearchModule } from '../search/search.module';
import { SearchIndexOutbox } from './entities/search-index-outbox.entity';
import { SearchSyncController } from './search-sync.controller';
import { SearchSyncProcessor } from './search-sync.processor';
import { SearchSyncService } from './search-sync.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([SearchIndexOutbox]),
    BullModule.registerQueue({ name: 'search-index-sync' }),
    SearchModule,
  ],
  controllers: [SearchSyncController],
  providers: [SearchSyncService, SearchSyncProcessor],
  exports: [SearchSyncService],
})
export class SearchSyncModule {}
