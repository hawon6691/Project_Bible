import { Module } from '@nestjs/common';
import { BullModule } from '@nestjs/bull';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { Seller } from '../seller/entities/seller.entity';
import { ProductSpec } from '../spec/entities/product-spec.entity';
import { SpecDefinition } from '../spec/entities/spec-definition.entity';
import { CrawlerController } from './crawler.controller';
import { CrawlerProcessor } from './crawler.processor';
import { CrawlerService } from './crawler.service';
import { CrawlerJob } from './entities/crawler-job.entity';
import { CrawlerRun } from './entities/crawler-run.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([CrawlerJob, CrawlerRun, Seller, Product, PriceEntry, SpecDefinition, ProductSpec]),
    BullModule.registerQueue({ name: 'crawler-collect' }),
  ],
  controllers: [CrawlerController],
  providers: [CrawlerService, CrawlerProcessor],
  exports: [CrawlerService],
})
export class CrawlerModule {}
