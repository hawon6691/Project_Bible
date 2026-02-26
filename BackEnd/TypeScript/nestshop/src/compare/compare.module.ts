import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Product } from '../product/entities/product.entity';
import { CompareController } from './compare.controller';
import { CompareService } from './compare.service';
import { CompareItem } from './entities/compare-item.entity';

@Module({
  imports: [TypeOrmModule.forFeature([CompareItem, Product])],
  controllers: [CompareController],
  providers: [CompareService],
  exports: [CompareService],
})
export class CompareModule {}
