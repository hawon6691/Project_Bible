import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SearchSyncModule } from '../search-sync/search-sync.module';
import { ProductController } from './product.controller';
import { ProductService } from './product.service';
import { Product } from './entities/product.entity';
import { ProductOption } from './entities/product-option.entity';
import { ProductImage } from './entities/product-image.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([Product, ProductOption, ProductImage]),
    SearchSyncModule,
  ],
  controllers: [ProductController],
  providers: [ProductService],
  exports: [ProductService],
})
export class ProductModule {}
