package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.InMemoryAuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.InMemoryActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.InMemoryAddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.InMemoryCartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.InMemoryCategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.InMemoryChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.CommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.InMemoryCommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.InMemoryCrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthCheckResult
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.DealRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InMemoryInquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.InMemoryOrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.InMemoryPaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.PaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.InMemoryPointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.PointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.InMemoryPredictionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.PredictionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.InMemoryPriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.InMemoryProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.InMemoryPushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.PushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.InMemoryRankingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.RankingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.InMemoryRecommendationRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.RecommendationRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.InMemoryReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.InMemorySearchRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.InMemorySellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.InMemorySpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.SpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.InMemorySupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.SupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.InMemoryTrustRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.TrustRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.InMemoryUserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.InMemoryWishlistRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.WishlistRepository
import io.ktor.client.request.header
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder

internal fun ApplicationTestBuilder.installPbShopApp(
    dbResult: DbHealthCheckResult =
        DbHealthCheckResult.up(
            engine = "postgresql",
            database = "pbdb",
        ),
    authRepository: AuthRepository = InMemoryAuthRepository.seeded(),
    userRepository: UserRepository = InMemoryUserRepository.seeded(),
    categoryRepository: CategoryRepository = InMemoryCategoryRepository.seeded(),
    productRepository: ProductRepository = InMemoryProductRepository.seeded(),
    specRepository: SpecRepository = InMemorySpecRepository.seeded(),
    sellerRepository: SellerRepository = InMemorySellerRepository.seeded(),
    priceRepository: PriceRepository = InMemoryPriceRepository.seeded(),
    cartRepository: CartRepository = InMemoryCartRepository.seeded(),
    addressRepository: AddressRepository = InMemoryAddressRepository.seeded(),
    orderRepository: OrderRepository = InMemoryOrderRepository.seeded(),
    paymentRepository: PaymentRepository = InMemoryPaymentRepository.seeded(),
    reviewRepository: ReviewRepository = InMemoryReviewRepository.seeded(),
    wishlistRepository: WishlistRepository = InMemoryWishlistRepository.seeded(),
    pointRepository: PointRepository = InMemoryPointRepository.seeded(),
    communityRepository: CommunityRepository = InMemoryCommunityRepository.seeded(),
    inquiryRepository: InquiryRepository = InMemoryInquiryRepository.seeded(),
    supportRepository: SupportRepository = InMemorySupportRepository.seeded(),
    activityRepository: ActivityRepository = InMemoryActivityRepository.seeded(),
    chatRepository: ChatRepository = InMemoryChatRepository.seeded(),
    pushRepository: PushRepository = InMemoryPushRepository.seeded(),
    rankingRepository: RankingRepository = InMemoryRankingRepository.seeded(),
    recommendationRepository: RecommendationRepository = InMemoryRecommendationRepository.seeded(),
    dealRepository: DealRepository = com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.InMemoryDealRepository.seeded(),
    searchRepository: SearchRepository = InMemorySearchRepository.seeded(),
    crawlerRepository: CrawlerRepository = InMemoryCrawlerRepository.seeded(),
    predictionRepository: PredictionRepository = InMemoryPredictionRepository.seeded(),
    trustRepository: TrustRepository = InMemoryTrustRepository.seeded(),
    generalPerMinute: Int = 60,
    authPerMinute: Int = 10,
) {
    environment {
        config = createTestConfig(generalPerMinute = generalPerMinute, authPerMinute = authPerMinute)
    }

    application {
        module(
            dbHealthService = FakeDbHealthService(dbResult),
            authRepository = authRepository,
            userRepository = userRepository,
            categoryRepository = categoryRepository,
            productRepository = productRepository,
            specRepository = specRepository,
            sellerRepository = sellerRepository,
            priceRepository = priceRepository,
            cartRepository = cartRepository,
            addressRepository = addressRepository,
            orderRepository = orderRepository,
            paymentRepository = paymentRepository,
            reviewRepository = reviewRepository,
            wishlistRepository = wishlistRepository,
            pointRepository = pointRepository,
            communityRepository = communityRepository,
            inquiryRepository = inquiryRepository,
            supportRepository = supportRepository,
            activityRepository = activityRepository,
            chatRepository = chatRepository,
            pushRepository = pushRepository,
            rankingRepository = rankingRepository,
            recommendationRepository = recommendationRepository,
            dealRepository = dealRepository,
            searchRepository = searchRepository,
            crawlerRepository = crawlerRepository,
            predictionRepository = predictionRepository,
            trustRepository = trustRepository,
        )
    }
}

internal fun createTestConfig(
    generalPerMinute: Int = 60,
    authPerMinute: Int = 10,
): MapApplicationConfig =
    MapApplicationConfig(
        "pbshop.apiPrefix" to "/api/v1",
        "pbshop.appName" to "pbshop-kotlin-ktor-gradle-exposeddao-postgresql",
        "pbshop.docsPath" to "/docs",
        "pbshop.baselineTrack" to "kotlin-ktor-gradle-exposeddao-postgresql",
        "pbshop.environment" to "test",
        "pbshop.rateLimit.generalPerMinute" to generalPerMinute.toString(),
        "pbshop.rateLimit.authPerMinute" to authPerMinute.toString(),
        "pbshop.security.requestIdHeader" to "X-Request-Id",
        "pbshop.observability.traceBufferLimit" to "50",
        "pbshop.database.url" to "jdbc:postgresql://127.0.0.1:5432/pbdb",
        "pbshop.database.username" to "project_bible",
        "pbshop.database.password" to "project_bible",
        "pbshop.database.driver" to "org.postgresql.Driver",
        "pbshop.database.engine" to "postgresql",
        "pbshop.database.database" to "pbdb",
        "pbshop.database.maxPoolSize" to "5",
        "pbshop.database.minIdle" to "1",
    )

internal fun io.ktor.client.request.HttpRequestBuilder.pbHeaders(
    role: String? = null,
    clientId: String = "test-client",
    requestId: String = "req-test",
) {
    header("X-Client-Id", clientId)
    header("X-Request-Id", requestId)
    if (role != null) {
        header("X-Role", role)
    }
}

private class FakeDbHealthService(
    private val result: DbHealthCheckResult,
) : DbHealthService {
    override fun check(): DbHealthCheckResult = result
}
