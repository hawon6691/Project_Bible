import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Seller } from '../seller/entities/seller.entity';
import { CrawlerController } from './crawler.controller';
import { CrawlerService } from './crawler.service';
import { CrawlerJob } from './entities/crawler-job.entity';
import { CrawlerRun } from './entities/crawler-run.entity';

@Module({
  imports: [TypeOrmModule.forFeature([CrawlerJob, CrawlerRun, Seller])],
  controllers: [CrawlerController],
  providers: [CrawlerService],
  exports: [CrawlerService],
})
export class CrawlerModule {}
