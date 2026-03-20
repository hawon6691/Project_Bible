import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PcBuildPart } from '../pc-builder/entities/pc-build-part.entity';
import { PcBuild } from '../pc-builder/entities/pc-build.entity';
import { PriceHistory } from '../price/entities/price-history.entity';
import { Product } from '../product/entities/product.entity';
import { UsedMarketController } from './used-market.controller';
import { UsedMarketService } from './used-market.service';

@Module({
  imports: [TypeOrmModule.forFeature([Product, PriceHistory, PcBuild, PcBuildPart])],
  controllers: [UsedMarketController],
  providers: [UsedMarketService],
  exports: [UsedMarketService],
})
export class UsedMarketModule {}
