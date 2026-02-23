import { BullModule } from '@nestjs/bull';
import { Module } from '@nestjs/common';
import { QueueAdminController } from './queue-admin.controller';
import { QueueAdminService } from './queue-admin.service';

@Module({
  imports: [
    BullModule.registerQueue(
      { name: 'activity-log' },
      { name: 'video-transcode' },
      { name: 'crawler-collect' },
      { name: 'search-index-sync' },
    ),
  ],
  controllers: [QueueAdminController],
  providers: [QueueAdminService],
})
export class QueueAdminModule {}
