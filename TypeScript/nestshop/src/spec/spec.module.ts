import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SpecController, ProductSpecController } from './spec.controller';
import { SpecService } from './spec.service';
import { SpecDefinition } from './entities/spec-definition.entity';
import { ProductSpec } from './entities/product-spec.entity';
import { SpecScore } from './entities/spec-score.entity';
import { Product } from '../product/entities/product.entity';
import { SpecController, ProductSpecController } from './spec.controller';
import { SpecService } from './spec.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([SpecDefinition, ProductSpec, SpecScore, Product]),
  ],
  controllers: [SpecController, ProductSpecController],
  providers: [SpecService],
  exports: [SpecService],
})
export class SpecModule {}
