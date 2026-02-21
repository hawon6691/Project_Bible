import { Module } from '@nestjs/common';
import { BullModule } from '@nestjs/bull';
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
import { ChatModule } from './chat/chat.module';
import { PushModule } from './push/push.module';
import { PredictionModule } from './prediction/prediction.module';
import { DealModule } from './deal/deal.module';
import { RecommendationModule } from './recommendation/recommendation.module';
import { RankingModule } from './ranking/ranking.module';
import { FraudModule } from './fraud/fraud.module';
import { TrustModule } from './trust/trust.module';
import { I18nModule } from './i18n/i18n.module';
import { ImageModule } from './image/image.module';
import { BadgeModule } from './badge/badge.module';
import { PcBuilderModule } from './pc-builder/pc-builder.module';
import { FriendModule } from './friend/friend.module';
import { VideoModule } from './video/video.module';
import { MediaModule } from './media/media.module';
import { NewsModule } from './news/news.module';
import { MatchingModule } from './matching/matching.module';
import { AnalyticsModule } from './analytics/analytics.module';
import { UsedMarketModule } from './used-market/used-market.module';
import { AutoModule } from './auto/auto.module';
import { AuctionModule } from './auction/auction.module';
import { CompareModule } from './compare/compare.module';
import { AdminSettingsModule } from './admin-settings/admin-settings.module';
import { HealthModule } from './health/health.module';
import { CrawlerModule } from './crawler/crawler.module';
import { SearchModule } from './search/search.module';

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

    // Bull Queue Redis 연결 (백그라운드 작업)
    BullModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => {
        const host =
          configService.get<string>('BULL_REDIS_HOST') ??
          configService.get<string>('REDIS_HOST', 'localhost');
        const port =
          configService.get<number>('BULL_REDIS_PORT') ??
          configService.get<number>('REDIS_PORT', 6379);
        const password = configService.get<string>('REDIS_PASSWORD');

        return {
          redis: { host, port, password },
        };
      },
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
    ChatModule,
    PushModule,
    PredictionModule,
    DealModule,
    RecommendationModule,
    RankingModule,
    FraudModule,
    TrustModule,
    I18nModule,
    ImageModule,
    BadgeModule,
    PcBuilderModule,
    FriendModule,
    VideoModule,
    MediaModule,
    NewsModule,
    MatchingModule,
    AnalyticsModule,
    UsedMarketModule,
    AutoModule,
    AuctionModule,
    CompareModule,
    AdminSettingsModule,
    HealthModule,
    CrawlerModule,
    SearchModule,
  ],
})
export class AppModule {}






























