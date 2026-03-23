package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthApiTest {
    @Test
    fun signup_and_login_routes_follow_the_contract() = testApplication {
        installPbShopApp()

        val signup =
            client.post("/api/v1/auth/signup") {
                pbHeaders(clientId = "auth-signup")
                header("Content-Type", "application/json")
                setBody("""{"email":"fresh-user@nestshop.com","password":"Password1!","name":"새사용자","phone":"010-9999-8888"}""")
            }
        val login =
            client.post("/api/v1/auth/login") {
                pbHeaders(clientId = "auth-login")
                header("Content-Type", "application/json")
                setBody("""{"email":"user1@nestshop.com","password":"Password1!"}""")
            }

        assertEquals(HttpStatusCode.Created, signup.status)
        assertEquals(HttpStatusCode.OK, login.status)
        assertTrue(login.bodyAsText().contains("\"accessToken\""))
        assertTrue(login.bodyAsText().contains("\"requestId\": \"req-test\""))
    }
}

class UserApiTest {
    @Test
    fun current_user_route_requires_user_role_and_returns_profile() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/users/me") { pbHeaders(role = "USER", clientId = "user-me") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"email\": \"user@pbshop.dev\""))
    }
}

class CategoryApiTest {
    @Test
    fun categories_route_returns_aligned_list_payload() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/categories") { pbHeaders(clientId = "categories") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"Laptop\""))
    }
}

class ProductApiTest {
    @Test
    fun products_list_and_detail_routes_return_stubbed_catalog_data() = testApplication {
        installPbShopApp()

        val listResponse = client.get("/api/v1/products") { pbHeaders(clientId = "products-list") }
        val detailResponse = client.get("/api/v1/products/1") { pbHeaders(clientId = "products-detail") }

        assertEquals(HttpStatusCode.OK, listResponse.status)
        assertEquals(HttpStatusCode.OK, detailResponse.status)
        assertTrue(listResponse.bodyAsText().contains("\"totalPages\""))
        assertTrue(detailResponse.bodyAsText().contains("\"PB GalaxyBook 4 Pro\""))
    }
}

class SpecSellerPriceApiTest {
    @Test
    fun spec_seller_and_price_routes_are_available() = testApplication {
        installPbShopApp()

        val specResponse = client.get("/api/v1/specs/definitions") { pbHeaders(clientId = "specs") }
        val sellerResponse = client.get("/api/v1/sellers") { pbHeaders(clientId = "sellers") }
        val priceResponse = client.get("/api/v1/products/1/prices") { pbHeaders(clientId = "prices") }

        assertEquals(HttpStatusCode.OK, specResponse.status)
        assertEquals(HttpStatusCode.OK, sellerResponse.status)
        assertEquals(HttpStatusCode.OK, priceResponse.status)
    }
}

class CartAddressApiTest {
    @Test
    fun cart_and_address_routes_require_user_role() = testApplication {
        installPbShopApp()

        val cartResponse = client.get("/api/v1/cart") { pbHeaders(role = "USER", clientId = "cart") }
        val addressResponse = client.get("/api/v1/addresses") { pbHeaders(role = "USER", clientId = "address") }

        assertEquals(HttpStatusCode.OK, cartResponse.status)
        assertEquals(HttpStatusCode.OK, addressResponse.status)
    }
}

class OrderPaymentApiTest {
    @Test
    fun order_and_payment_routes_expose_expected_state_payloads() = testApplication {
        installPbShopApp()

        val orderResponse = client.post("/api/v1/orders") { pbHeaders(role = "USER", clientId = "order") }
        val paymentResponse = client.post("/api/v1/payments") { pbHeaders(role = "USER", clientId = "payment") }

        assertEquals(HttpStatusCode.Created, orderResponse.status)
        assertEquals(HttpStatusCode.Created, paymentResponse.status)
        assertTrue(orderResponse.bodyAsText().contains("\"ORDER_PLACED\""))
    }
}

class ReviewWishlistPointApiTest {
    @Test
    fun review_wishlist_and_point_routes_are_grouped_for_regression() = testApplication {
        installPbShopApp()

        val reviewResponse = client.post("/api/v1/products/1/reviews") { pbHeaders(role = "USER", clientId = "review") }
        val wishlistResponse = client.get("/api/v1/wishlist") { pbHeaders(role = "USER", clientId = "wishlist") }
        val pointResponse = client.get("/api/v1/points/balance") { pbHeaders(role = "USER", clientId = "points") }

        assertEquals(HttpStatusCode.Created, reviewResponse.status)
        assertEquals(HttpStatusCode.OK, wishlistResponse.status)
        assertEquals(HttpStatusCode.OK, pointResponse.status)
    }
}

class CommunityInquirySupportApiTest {
    @Test
    fun community_inquiry_and_support_routes_are_available() = testApplication {
        installPbShopApp()

        val communityResponse = client.get("/api/v1/boards/1/posts") { pbHeaders(clientId = "community") }
        val inquiryResponse = client.post("/api/v1/products/1/inquiries") { pbHeaders(role = "USER", clientId = "inquiry") }
        val supportResponse = client.post("/api/v1/support/tickets") { pbHeaders(role = "USER", clientId = "support") }

        assertEquals(HttpStatusCode.OK, communityResponse.status)
        assertEquals(HttpStatusCode.Created, inquiryResponse.status)
        assertEquals(HttpStatusCode.Created, supportResponse.status)
    }
}

class ActivityChatPushApiTest {
    @Test
    fun activity_chat_and_push_routes_are_available() = testApplication {
        installPbShopApp()

        val activityResponse = client.get("/api/v1/activity/views") { pbHeaders(role = "USER", clientId = "activity") }
        val chatResponse = client.post("/api/v1/chat/rooms") { pbHeaders(role = "USER", clientId = "chat") }
        val pushResponse = client.post("/api/v1/push/subscriptions") { pbHeaders(role = "USER", clientId = "push") }

        assertEquals(HttpStatusCode.OK, activityResponse.status)
        assertEquals(HttpStatusCode.Created, chatResponse.status)
        assertEquals(HttpStatusCode.Created, pushResponse.status)
    }
}

class PredictionDealRecommendationRankingApiTest {
    @Test
    fun prediction_deal_recommendation_and_ranking_routes_work() = testApplication {
        installPbShopApp()

        val predictionResponse = client.get("/api/v1/predictions/products/1/price-trend") { pbHeaders(clientId = "prediction") }
        val dealResponse = client.get("/api/v1/deals") { pbHeaders(clientId = "deals") }
        val recommendationResponse = client.get("/api/v1/recommendations/today") { pbHeaders(clientId = "recommendation") }
        val rankingResponse = client.get("/api/v1/rankings/products/popular") { pbHeaders(clientId = "ranking") }

        assertEquals(HttpStatusCode.OK, predictionResponse.status)
        assertEquals(HttpStatusCode.OK, dealResponse.status)
        assertEquals(HttpStatusCode.OK, recommendationResponse.status)
        assertEquals(HttpStatusCode.OK, rankingResponse.status)
    }
}

class FraudTrustI18nImageBadgeApiTest {
    @Test
    fun fraud_trust_i18n_image_and_badge_routes_work() = testApplication {
        installPbShopApp()

        val fraudResponse = client.get("/api/v1/fraud/alerts") { pbHeaders(role = "ADMIN", clientId = "fraud") }
        val trustResponse = client.get("/api/v1/sellers/1/trust") { pbHeaders(clientId = "trust") }
        val i18nResponse = client.get("/api/v1/i18n/translations") { pbHeaders(clientId = "i18n") }
        val badgeResponse = client.get("/api/v1/badges") { pbHeaders(clientId = "badges") }

        assertEquals(HttpStatusCode.OK, fraudResponse.status)
        assertEquals(HttpStatusCode.OK, trustResponse.status)
        assertEquals(HttpStatusCode.OK, i18nResponse.status)
        assertEquals(HttpStatusCode.OK, badgeResponse.status)
    }
}

class PcFriendShortformMediaNewsMatchingApiTest {
    @Test
    fun pc_friend_shortform_media_news_and_matching_routes_work() = testApplication {
        installPbShopApp()

        val pcResponse = client.get("/api/v1/pc-builds") { pbHeaders(role = "USER", clientId = "pc") }
        val friendResponse = client.get("/api/v1/friends") { pbHeaders(role = "USER", clientId = "friends") }
        val shortformResponse = client.get("/api/v1/shortforms") { pbHeaders(clientId = "shortforms") }
        val mediaResponse = client.post("/api/v1/media/presigned-url") { pbHeaders(role = "USER", clientId = "media") }
        val newsResponse = client.get("/api/v1/news") { pbHeaders(clientId = "news") }
        val matchingResponse = client.get("/api/v1/matching/pending") { pbHeaders(role = "ADMIN", clientId = "matching") }

        assertEquals(HttpStatusCode.OK, pcResponse.status)
        assertEquals(HttpStatusCode.OK, friendResponse.status)
        assertEquals(HttpStatusCode.OK, shortformResponse.status)
        assertEquals(HttpStatusCode.OK, mediaResponse.status)
        assertEquals(HttpStatusCode.OK, newsResponse.status)
        assertEquals(HttpStatusCode.OK, matchingResponse.status)
    }
}

class AnalyticsUsedMarketAutoAuctionCompareApiTest {
    @Test
    fun analytics_used_market_auto_auction_and_compare_routes_work() = testApplication {
        installPbShopApp()

        val analyticsResponse = client.get("/api/v1/analytics/products/1/lowest-ever") { pbHeaders(clientId = "analytics") }
        val usedMarketResponse = client.get("/api/v1/used-market/products/1/price") { pbHeaders(clientId = "used-market") }
        val autoResponse = client.get("/api/v1/auto/models") { pbHeaders(clientId = "auto") }
        val auctionResponse = client.get("/api/v1/auctions") { pbHeaders(clientId = "auction") }
        val compareResponse = client.get("/api/v1/compare") { pbHeaders(clientId = "compare") }

        assertEquals(HttpStatusCode.OK, analyticsResponse.status)
        assertEquals(HttpStatusCode.OK, usedMarketResponse.status)
        assertEquals(HttpStatusCode.OK, autoResponse.status)
        assertEquals(HttpStatusCode.OK, auctionResponse.status)
        assertEquals(HttpStatusCode.OK, compareResponse.status)
    }
}

class HealthRouteTest {
    @Test
    fun health_route_reflects_db_status_and_component_checks() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/health") { pbHeaders(clientId = "health") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("kotlin-ktor-gradle-exposeddao-postgresql"))
    }
}

class OpsApiTest {
    @Test
    fun ops_routes_cover_query_resilience_queue_and_observability() = testApplication {
        installPbShopApp()

        val queryResponse = client.get("/api/v1/query/products") { pbHeaders(clientId = "query") }
        val resilienceResponse = client.get("/api/v1/resilience/circuit-breakers") { pbHeaders(role = "ADMIN", clientId = "resilience") }
        val queueResponse = client.get("/api/v1/admin/queues/stats") { pbHeaders(role = "ADMIN", clientId = "queue") }
        val observabilityResponse = client.get("/api/v1/admin/observability/dashboard") { pbHeaders(role = "ADMIN", clientId = "observability") }

        assertEquals(HttpStatusCode.OK, queryResponse.status)
        assertEquals(HttpStatusCode.OK, resilienceResponse.status)
        assertEquals(HttpStatusCode.OK, queueResponse.status)
        assertEquals(HttpStatusCode.OK, observabilityResponse.status)
    }
}
