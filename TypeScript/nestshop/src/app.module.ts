import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CommonModule } from './common/common.module';
import { AuthModule } from './auth/auth.module';
import { UserModule } from './user/user.module';
import { CategoryModule } from './category/category.module';
import { ProductModule } from './product/product.module';
import { SpecModule } from './spec/spec.module';
import { SellerModule } from './seller/seller.module';
import { PriceModule } from './price/price.module';
import { CartModule } from './cart/cart.module';
import { OrderModule } from './order/order.module';
import { ReviewModule } from './review/review.module';
import { PointModule } from './point/point.module';
import { WishlistModule } from './wishlist/wishlist.module';
import { CommunityModule } from './community/community.module';
import { AddressModule } from './address/address.module';
import { InquiryModule } from './inquiry/inquiry.module';
import { SupportModule } from './support/support.module';
import { FaqModule } from './faq/faq.module';
import { ActivityModule } from './activity/activity.module';

@Module({
  imports: [
    // 환경변수 설정
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
    }),

    // TypeORM PostgreSQL 연결
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: configService.get<string>('DB_HOST', 'localhost'),
        port: configService.get<number>('DB_PORT', 5432),
        username: configService.get<string>('DB_USERNAME', 'postgres'),
        password: configService.get<string>('DB_PASSWORD', 'postgres'),
        database: configService.get<string>('DB_DATABASE', 'nestshop'),
        autoLoadEntities: true,
        synchronize: configService.get<boolean>('DB_SYNCHRONIZE', false),
        logging: configService.get<boolean>('DB_LOGGING', false),
      }),
    }),

    // 공통 모듈 (전역 가드, 필터, 인터셉터)
    CommonModule,

    // 기능 모듈
    AuthModule,
    UserModule,
    CategoryModule,
    ProductModule,
    SpecModule,
    SellerModule,
    PriceModule,
    CartModule,
    OrderModule,
    ReviewModule,
    PointModule,
    WishlistModule,
    CommunityModule,
    AddressModule,
    InquiryModule,
    SupportModule,
    FaqModule,
    ActivityModule,
  ],
})
export class AppModule {}



