import { Module } from '@nestjs/common';
import { ElasticsearchModule } from '@nestjs/elasticsearch';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { SearchController } from './search.controller';
import { SearchService } from './search.service';
import { SearchLog } from './entities/search-log.entity';
import { SearchRecentKeyword } from './entities/search-recent-keyword.entity';
import { SearchWeightSetting } from './entities/search-weight-setting.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([Product, PriceEntry, User, SearchLog, SearchRecentKeyword, SearchWeightSetting]),
    ElasticsearchModule.register({
      // ES 8 서버와 elasticsearch-js 9 클라이언트를 함께 쓸 때 호환 헤더를 명시한다.
      headers: {
        accept: 'application/vnd.elasticsearch+json; compatible-with=8',
        'content-type': 'application/vnd.elasticsearch+json; compatible-with=8',
      },
      node: process.env.ELASTICSEARCH_NODE ?? 'http://localhost:9200',
      auth:
        process.env.ELASTICSEARCH_USERNAME && process.env.ELASTICSEARCH_PASSWORD
          ? {
              username: process.env.ELASTICSEARCH_USERNAME,
              password: process.env.ELASTICSEARCH_PASSWORD,
            }
          : undefined,
    }),
  ],
  controllers: [SearchController],
  providers: [SearchService],
  exports: [SearchService],
})
export class SearchModule {}
