import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Product } from '../product/entities/product.entity';
import { MatchingController } from './matching.controller';
import { MatchingService } from './matching.service';
import { ProductMapping } from './entities/product-mapping.entity';

@Module({
  imports: [TypeOrmModule.forFeature([ProductMapping, Product])],
  controllers: [MatchingController],
  providers: [MatchingService],
  exports: [MatchingService],
})
export class MatchingModule {}
