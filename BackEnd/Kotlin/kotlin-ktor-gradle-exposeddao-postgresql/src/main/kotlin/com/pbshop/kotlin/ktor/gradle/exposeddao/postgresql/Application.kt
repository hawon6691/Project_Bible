package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings.AdminSettingsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings.ExposedDaoAdminSettingsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.ExposedDaoAuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ExposedDaoActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.ExposedDaoAddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics.AnalyticsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics.ExposedDaoAnalyticsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto.AutoRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto.ExposedDaoAutoRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction.AuctionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction.ExposedDaoAuctionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.ExposedDaoCartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.ExposedDaoCategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ExposedDaoChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.CommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.ExposedDaoCommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare.CompareRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare.ExposedDaoCompareRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.ExposedDaoCrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.JdbcDbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.DealRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.ExposedDaoDealRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode.CatalogErrorCodeRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode.ErrorCodeRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend.ExposedDaoFriendRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend.FriendRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud.ExposedDaoFraudRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud.FraudRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.ExposedDaoInquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n.ExposedDaoI18nRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n.I18nRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ExposedDaoImageRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ImageRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching.ExposedDaoMatchingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching.MatchingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.ExposedDaoMediaRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news.ExposedDaoNewsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news.NewsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability.InMemoryObservabilityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability.ObservabilityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability.ObservabilityRuntimeRegistry
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard.InMemoryOpsDashboardRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard.OpsDashboardRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.ExposedDaoOrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.ExposedDaoPaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.PaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder.ExposedDaoPcBuilderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder.PcBuilderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.ExposedDaoPointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.PointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.ExposedDaoPredictionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.PredictionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureHttp
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureRouting
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureSerialization
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureWebSockets
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.ExposedDaoPriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ExposedDaoProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.ExposedDaoPushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.PushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query.ExposedDaoQueryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query.QueryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.ExposedDaoQueueAdminRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueAdminRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.ExposedDaoRankingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.RankingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.ExposedDaoRecommendationRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.RecommendationRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.InMemoryResilienceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.ResilienceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ExposedDaoReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.ExposedDaoSearchRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.ExposedDaoSellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform.ExposedDaoShortformRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform.ShortformRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.ExposedDaoSpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.SpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.ExposedDaoSupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.SupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.ExposedDaoTrustRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.TrustRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket.ExposedDaoUsedMarketRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket.UsedMarketRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.ExposedDaoUserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.ExposedDaoWishlistRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.WishlistRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge.BadgeRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge.ExposedDaoBadgeRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = PbShopConfig.from(environment.config)
    val databaseFactory = DatabaseFactory(config.database)
    databaseFactory.initialize()
    module(
        dbHealthService = JdbcDbHealthService(
            databaseFactory = databaseFactory,
            config = config.database,
        ),
        authRepository = ExposedDaoAuthRepository(databaseFactory),
        userRepository = ExposedDaoUserRepository(databaseFactory),
        categoryRepository = ExposedDaoCategoryRepository(databaseFactory),
        productRepository = ExposedDaoProductRepository(databaseFactory),
        specRepository = ExposedDaoSpecRepository(databaseFactory),
        sellerRepository = ExposedDaoSellerRepository(databaseFactory),
        priceRepository = ExposedDaoPriceRepository(databaseFactory),
        cartRepository = ExposedDaoCartRepository(databaseFactory),
        addressRepository = ExposedDaoAddressRepository(databaseFactory),
        orderRepository = ExposedDaoOrderRepository(databaseFactory),
        paymentRepository = ExposedDaoPaymentRepository(databaseFactory),
        reviewRepository = ExposedDaoReviewRepository(databaseFactory),
        wishlistRepository = ExposedDaoWishlistRepository(databaseFactory),
        pointRepository = ExposedDaoPointRepository(databaseFactory),
        communityRepository = ExposedDaoCommunityRepository(databaseFactory),
        inquiryRepository = ExposedDaoInquiryRepository(databaseFactory),
        supportRepository = ExposedDaoSupportRepository(databaseFactory),
        activityRepository = ExposedDaoActivityRepository(databaseFactory),
        chatRepository = ExposedDaoChatRepository(databaseFactory),
        pushRepository = ExposedDaoPushRepository(databaseFactory),
        rankingRepository = ExposedDaoRankingRepository(databaseFactory),
        recommendationRepository = ExposedDaoRecommendationRepository(databaseFactory),
        dealRepository = ExposedDaoDealRepository(databaseFactory),
        searchRepository = ExposedDaoSearchRepository(databaseFactory),
        crawlerRepository = ExposedDaoCrawlerRepository(databaseFactory),
        predictionRepository = ExposedDaoPredictionRepository(databaseFactory),
        trustRepository = ExposedDaoTrustRepository(databaseFactory),
        i18nRepository = ExposedDaoI18nRepository(databaseFactory),
        imageRepository = ExposedDaoImageRepository(databaseFactory),
        mediaRepository = ExposedDaoMediaRepository(databaseFactory),
        badgeRepository = ExposedDaoBadgeRepository(databaseFactory),
        pcBuilderRepository = ExposedDaoPcBuilderRepository(databaseFactory),
        friendRepository = ExposedDaoFriendRepository(databaseFactory),
        shortformRepository = ExposedDaoShortformRepository(databaseFactory),
        newsRepository = ExposedDaoNewsRepository(databaseFactory),
        matchingRepository = ExposedDaoMatchingRepository(databaseFactory),
        fraudRepository = ExposedDaoFraudRepository(databaseFactory),
        analyticsRepository = ExposedDaoAnalyticsRepository(databaseFactory),
        usedMarketRepository = ExposedDaoUsedMarketRepository(databaseFactory),
        autoRepository = ExposedDaoAutoRepository(databaseFactory),
        auctionRepository = ExposedDaoAuctionRepository(databaseFactory),
        compareRepository = ExposedDaoCompareRepository(databaseFactory),
        adminSettingsRepository = ExposedDaoAdminSettingsRepository(databaseFactory),
        resilienceRepository = InMemoryResilienceRepository.seeded(),
        errorCodeRepository = CatalogErrorCodeRepository(),
        queueAdminRepository = ExposedDaoQueueAdminRepository(databaseFactory),
        queryRepository = ExposedDaoQueryRepository(databaseFactory),
        observabilityRepository = InMemoryObservabilityRepository(config.observability.traceBufferLimit),
        opsDashboardRepository = InMemoryOpsDashboardRepository(),
    )
}

fun Application.module(
    dbHealthService: DbHealthService,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    categoryRepository: CategoryRepository,
    productRepository: ProductRepository,
    specRepository: SpecRepository,
    sellerRepository: SellerRepository,
    priceRepository: PriceRepository,
    cartRepository: CartRepository,
    addressRepository: AddressRepository,
    orderRepository: OrderRepository,
    paymentRepository: PaymentRepository,
    reviewRepository: ReviewRepository,
    wishlistRepository: WishlistRepository,
    pointRepository: PointRepository,
    communityRepository: CommunityRepository,
    inquiryRepository: InquiryRepository,
    supportRepository: SupportRepository,
    activityRepository: ActivityRepository,
    chatRepository: ChatRepository,
    pushRepository: PushRepository,
    rankingRepository: RankingRepository,
    recommendationRepository: RecommendationRepository,
    dealRepository: DealRepository,
    searchRepository: SearchRepository,
    crawlerRepository: CrawlerRepository,
    predictionRepository: PredictionRepository,
    trustRepository: TrustRepository,
    i18nRepository: I18nRepository,
    imageRepository: ImageRepository,
    mediaRepository: MediaRepository,
    badgeRepository: BadgeRepository,
    pcBuilderRepository: PcBuilderRepository,
    friendRepository: FriendRepository,
    shortformRepository: ShortformRepository,
    newsRepository: NewsRepository,
    matchingRepository: MatchingRepository,
    fraudRepository: FraudRepository,
    analyticsRepository: AnalyticsRepository,
    usedMarketRepository: UsedMarketRepository,
    autoRepository: AutoRepository,
    auctionRepository: AuctionRepository,
    compareRepository: CompareRepository,
    adminSettingsRepository: AdminSettingsRepository,
    resilienceRepository: ResilienceRepository,
    errorCodeRepository: ErrorCodeRepository,
    queueAdminRepository: QueueAdminRepository,
    queryRepository: QueryRepository,
    observabilityRepository: ObservabilityRepository,
    opsDashboardRepository: OpsDashboardRepository,
) {
    val config = PbShopConfig.from(environment.config)

    install(CallLogging) {
        level = Level.INFO
    }

    ObservabilityRuntimeRegistry.repository = observabilityRepository
    configureSerialization()
    configureHttp(config)
    configureWebSockets()
    configureRouting(
        config,
        dbHealthService,
        authRepository,
        userRepository,
        categoryRepository,
        productRepository,
        specRepository,
        sellerRepository,
        priceRepository,
        cartRepository,
        addressRepository,
        orderRepository,
        paymentRepository,
        reviewRepository,
        wishlistRepository,
        pointRepository,
        communityRepository,
        inquiryRepository,
        supportRepository,
        activityRepository,
        chatRepository,
        pushRepository,
        rankingRepository,
        recommendationRepository,
        dealRepository,
        searchRepository,
        crawlerRepository,
        predictionRepository,
        trustRepository,
        i18nRepository,
        imageRepository,
        mediaRepository,
        badgeRepository,
        pcBuilderRepository,
        friendRepository,
        shortformRepository,
        newsRepository,
        matchingRepository,
        fraudRepository,
        analyticsRepository,
        usedMarketRepository,
        autoRepository,
        auctionRepository,
        compareRepository,
        adminSettingsRepository,
        resilienceRepository,
        errorCodeRepository,
        queueAdminRepository,
        queryRepository,
        observabilityRepository,
        opsDashboardRepository,
    )
}
