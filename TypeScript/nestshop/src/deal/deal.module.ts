import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Product } from '../product/entities/product.entity';
import { DealController } from './deal.controller';
import { DealService } from './deal.service';
import { Deal } from './entities/deal.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Deal, Product])],
  controllers: [DealController],
  providers: [DealService],
  exports: [DealService],
})
export class DealModule {}
