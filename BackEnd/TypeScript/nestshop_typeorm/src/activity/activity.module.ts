import { Module } from '@nestjs/common';
import { BullModule } from '@nestjs/bull';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Order } from '../order/entities/order.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { ActivityController } from './activity.controller';
import { ActivityProcessor } from './activity.processor';
import { ActivityService } from './activity.service';
import { RecentProductView } from './entities/recent-product-view.entity';
import { SearchHistory } from './entities/search-history.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([RecentProductView, SearchHistory, Product, Order, User]),
    BullModule.registerQueue({ name: 'activity-log' }),
  ],
  controllers: [ActivityController],
  providers: [ActivityService, ActivityProcessor],
  exports: [ActivityService],
})
export class ActivityModule {}
