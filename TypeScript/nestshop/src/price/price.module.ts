import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PriceController } from './price.controller';
import { PriceService } from './price.service';
import { PriceEntry } from './entities/price-entry.entity';
import { PriceHistory } from './entities/price-history.entity';
import { PriceAlert } from './entities/price-alert.entity';
import { Product } from '../product/entities/product.entity';

@Module({
  imports: [TypeOrmModule.forFeature([PriceEntry, PriceHistory, PriceAlert, Product])],
  controllers: [PriceController],
  providers: [PriceService],
  exports: [PriceService],
})
export class PriceModule {}
