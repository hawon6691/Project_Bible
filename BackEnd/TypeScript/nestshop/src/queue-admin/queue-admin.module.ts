import { BullModule } from '@nestjs/bull';
import { Module } from '@nestjs/common';
import { QUEUE_NAMES } from '../common/constants/queue-names';
import { QueueAdminController } from './queue-admin.controller';
import { QueueAdminService } from './queue-admin.service';

@Module({
  imports: [
    BullModule.registerQueue(
      { name: QUEUE_NAMES.ACTIVITY_LOG },
      { name: QUEUE_NAMES.VIDEO_TRANSCODE },
      { name: QUEUE_NAMES.CRAWLER_COLLECT },
      { name: QUEUE_NAMES.SEARCH_INDEX_SYNC },
    ),
  ],
  controllers: [QueueAdminController],
  providers: [QueueAdminService],
  exports: [QueueAdminService],
})
export class QueueAdminModule {}
