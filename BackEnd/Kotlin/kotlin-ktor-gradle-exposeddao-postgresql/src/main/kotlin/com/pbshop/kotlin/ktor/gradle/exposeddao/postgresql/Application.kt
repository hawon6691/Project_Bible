package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.ExposedDaoAuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity.ExposedDaoActivityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.ExposedDaoAddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.ExposedDaoCartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.ExposedDaoCategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat.ExposedDaoChatRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.CommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community.ExposedDaoCommunityRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.JdbcDbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.ExposedDaoInquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry.InquiryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.ExposedDaoOrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.ExposedDaoPaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment.PaymentRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.ExposedDaoPointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point.PointRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureHttp
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureRouting
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureSerialization
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.ExposedDaoPriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ExposedDaoProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.ExposedDaoPushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push.PushRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ExposedDaoReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review.ReviewRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.ExposedDaoSellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.ExposedDaoSpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec.SpecRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.ExposedDaoSupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support.SupportRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.ExposedDaoUserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.ExposedDaoWishlistRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist.WishlistRepository
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
) {
    val config = PbShopConfig.from(environment.config)

    install(CallLogging) {
        level = Level.INFO
    }

    configureSerialization()
    configureHttp(config)
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
    )
}
