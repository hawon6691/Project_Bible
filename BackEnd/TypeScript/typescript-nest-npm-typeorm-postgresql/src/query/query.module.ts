import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { QueryController } from './query.controller';
import { QueryService } from './query.service';
import { ProductQueryView } from './entities/product-query-view.entity';

@Module({
  imports: [TypeOrmModule.forFeature([ProductQueryView, Product, PriceEntry])],
  controllers: [QueryController],
  providers: [QueryService],
  exports: [QueryService],
})
export class QueryModule {}
