package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ActivityController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ActivityService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings.AdminSettingsController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings.AdminSettingsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings.AdminSettingsService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics.AnalyticsController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics.AnalyticsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics.AnalyticsService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction.AuctionController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction.AuctionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction.AuctionService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto.AutoController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto.AutoRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto.AutoService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge.BadgeController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge.BadgeRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge.BadgeService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ChatController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ChatService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.CommunityController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.CommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.CommunityService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare.CompareController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare.CompareRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare.CompareService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.DealController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.DealRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal.DealService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.DocsController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.DocsService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.pbShopEndpointSpecs
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode.ErrorCodeController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode.ErrorCodeRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode.ErrorCodeService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend.FriendController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend.FriendRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend.FriendService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud.FraudController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud.FraudRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud.FraudService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.health.HealthController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.health.HealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n.I18nController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n.I18nRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n.I18nService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ImageController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ImageRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image.ImageService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InquiryController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InquiryService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching.MatchingController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching.MatchingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching.MatchingService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news.NewsController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news.NewsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news.NewsService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability.ObservabilityController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability.ObservabilityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability.ObservabilityService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard.OpsDashboardController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard.OpsDashboardRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard.OpsDashboardService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.PaymentController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.PaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.PaymentService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder.PcBuilderController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder.PcBuilderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder.PcBuilderService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform.PlatformController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform.PlatformService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.PointController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.PointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.PointService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.PredictionController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.PredictionRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction.PredictionService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.PushController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.PushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.PushService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query.QueryController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query.QueryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query.QueryService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueAdminController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueAdminRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueAdminService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.RankingController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.RankingRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking.RankingService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.RecommendationController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.RecommendationRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation.RecommendationService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.ResilienceController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.ResilienceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.ResilienceService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ReviewController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ReviewService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform.ShortformController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform.ShortformRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform.ShortformService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.SpecController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.SpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.SpecService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.SupportController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.SupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.SupportService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.TrustController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.TrustRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust.TrustService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket.UsedMarketController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket.UsedMarketRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket.UsedMarketService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.WishlistController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.WishlistRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.WishlistService
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    config: PbShopConfig,
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
) {
    val endpointSpecs = pbShopEndpointSpecs()
    val platformController = PlatformController(PlatformService(config, endpointSpecs))
    val healthController = HealthController(HealthService(config, dbHealthService))
    val docsController = DocsController(DocsService(config, endpointSpecs))

    val authController = AuthController(AuthService(authRepository))
    val userController = UserController(UserService(userRepository))
    val categoryController = CategoryController(CategoryService(categoryRepository))
    val productController = ProductController(ProductService(productRepository))
    val specController = SpecController(SpecService(specRepository))
    val sellerController = SellerController(SellerService(sellerRepository))
    val priceController = PriceController(PriceService(priceRepository))
    val cartController = CartController(CartService(cartRepository))
    val addressController = AddressController(AddressService(addressRepository))
    val orderController =
        OrderController(
            OrderService(
                repository = orderRepository,
                addressRepository = addressRepository,
                cartRepository = cartRepository,
                productRepository = productRepository,
                sellerRepository = sellerRepository,
                priceRepository = priceRepository,
            ),
        )
    val paymentController = PaymentController(PaymentService(paymentRepository, orderRepository))
    val reviewController = ReviewController(ReviewService(reviewRepository, orderRepository))
    val wishlistController = WishlistController(WishlistService(wishlistRepository))
    val pointController = PointController(PointService(pointRepository))
    val communityController = CommunityController(CommunityService(communityRepository))
    val inquiryController = InquiryController(InquiryService(inquiryRepository))
    val supportController = SupportController(SupportService(supportRepository))
    val activityController = ActivityController(ActivityService(ActivityRepository()))
    val chatController = ChatController(ChatService(ChatRepository()))
    val pushController = PushController(PushService(PushRepository()))
    val rankingController = RankingController(RankingService(RankingRepository()))
    val recommendationController = RecommendationController(RecommendationService(RecommendationRepository()))
    val dealController = DealController(DealService(DealRepository()))
    val searchController = SearchController(SearchService(SearchRepository()))
    val crawlerController = CrawlerController(CrawlerService(CrawlerRepository()))
    val predictionController = PredictionController(PredictionService(PredictionRepository()))
    val trustController = TrustController(TrustService(TrustRepository()))
    val i18nController = I18nController(I18nService(I18nRepository()))
    val imageController = ImageController(ImageService(ImageRepository()))
    val mediaController = MediaController(MediaService(MediaRepository()))
    val badgeController = BadgeController(BadgeService(BadgeRepository()))
    val pcBuilderController = PcBuilderController(PcBuilderService(PcBuilderRepository()))
    val friendController = FriendController(FriendService(FriendRepository()))
    val shortformController = ShortformController(ShortformService(ShortformRepository()))
    val newsController = NewsController(NewsService(NewsRepository()))
    val matchingController = MatchingController(MatchingService(MatchingRepository()))
    val fraudController = FraudController(FraudService(FraudRepository()))
    val analyticsController = AnalyticsController(AnalyticsService(AnalyticsRepository()))
    val usedMarketController = UsedMarketController(UsedMarketService(UsedMarketRepository()))
    val autoController = AutoController(AutoService(AutoRepository()))
    val auctionController = AuctionController(AuctionService(AuctionRepository()))
    val compareController = CompareController(CompareService(CompareRepository()))
    val adminSettingsController = AdminSettingsController(AdminSettingsService(AdminSettingsRepository()))
    val resilienceController = ResilienceController(ResilienceService(ResilienceRepository()))
    val errorCodeController = ErrorCodeController(ErrorCodeService(ErrorCodeRepository()))
    val queueAdminController = QueueAdminController(QueueAdminService(QueueAdminRepository()))
    val opsDashboardController = OpsDashboardController(OpsDashboardService(OpsDashboardRepository()))
    val observabilityController = ObservabilityController(ObservabilityService(ObservabilityRepository()))
    val queryController = QueryController(QueryService(QueryRepository()))

    routing {
        with(platformController) {
            registerPublicRoutes()
        }
        with(healthController) {
            registerPublicRoutes()
        }
        with(docsController) {
            registerPublicRoutes()
        }

        route(config.apiPrefix) {
            with(healthController) {
                registerApiRoutes()
            }
            with(docsController) {
                registerApiRoutes()
            }
            with(authController) { register() }
            with(userController) { register() }
            with(categoryController) { register() }
            with(productController) { register() }
            with(specController) { register() }
            with(sellerController) { register() }
            with(priceController) { register() }
            with(cartController) { register() }
            with(addressController) { register() }
            with(orderController) { register() }
            with(paymentController) { register() }
            with(reviewController) { register() }
            with(wishlistController) { register() }
            with(pointController) { register() }
            with(communityController) { register() }
            with(inquiryController) { register() }
            with(supportController) { register() }
            with(activityController) { register() }
            with(chatController) { register() }
            with(pushController) { register() }
            with(rankingController) { register() }
            with(recommendationController) { register() }
            with(dealController) { register() }
            with(searchController) { register() }
            with(crawlerController) { register() }
            with(predictionController) { register() }
            with(trustController) { register() }
            with(i18nController) { register() }
            with(imageController) { register() }
            with(mediaController) { register() }
            with(badgeController) { register() }
            with(pcBuilderController) { register() }
            with(friendController) { register() }
            with(shortformController) { register() }
            with(newsController) { register() }
            with(matchingController) { register() }
            with(fraudController) { register() }
            with(analyticsController) { register() }
            with(usedMarketController) { register() }
            with(autoController) { register() }
            with(auctionController) { register() }
            with(compareController) { register() }
            with(adminSettingsController) { register() }
            with(resilienceController) { register() }
            with(errorCodeController) { register() }
            with(queueAdminController) { register() }
            with(opsDashboardController) { register() }
            with(observabilityController) { register() }
            with(queryController) { register() }
        }
    }
}
