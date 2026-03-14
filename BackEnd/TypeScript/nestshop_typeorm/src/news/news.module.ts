import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Product } from '../product/entities/product.entity';
import { NewsController } from './news.controller';
import { NewsService } from './news.service';
import { NewsCategory } from './entities/news-category.entity';
import { NewsProduct } from './entities/news-product.entity';
import { News } from './entities/news.entity';

@Module({
  imports: [TypeOrmModule.forFeature([News, NewsCategory, NewsProduct, Product])],
  controllers: [NewsController],
  providers: [NewsService],
  exports: [NewsService],
})
export class NewsModule {}
