import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { PcBuilderController } from './pc-builder.controller';
import { PcBuilderService } from './pc-builder.service';
import { PcBuildPart } from './entities/pc-build-part.entity';
import { PcBuild } from './entities/pc-build.entity';
import { PcCompatibilityRule } from './entities/pc-compatibility-rule.entity';

@Module({
  imports: [TypeOrmModule.forFeature([PcBuild, PcBuildPart, PcCompatibilityRule, Product, PriceEntry, User])],
  controllers: [PcBuilderController],
  providers: [PcBuilderService],
  exports: [PcBuilderService],
})
export class PcBuilderModule {}
