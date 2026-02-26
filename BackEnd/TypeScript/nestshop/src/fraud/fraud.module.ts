import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { FraudController } from './fraud.controller';
import { FraudService } from './fraud.service';
import { FraudFlag } from './entities/fraud-flag.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Product, PriceEntry, FraudFlag])],
  controllers: [FraudController],
  providers: [FraudService],
  exports: [FraudService],
})
export class FraudModule {}
