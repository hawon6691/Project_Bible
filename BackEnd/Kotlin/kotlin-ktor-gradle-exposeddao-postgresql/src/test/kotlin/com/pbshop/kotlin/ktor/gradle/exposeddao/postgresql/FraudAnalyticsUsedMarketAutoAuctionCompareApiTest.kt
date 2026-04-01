package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FraudAnalyticsUsedMarketAutoAuctionCompareActualApiTest {
    @Test
    fun fraud_analytics_and_used_market_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val alerts = client.get("/api/v1/fraud/alerts?page=1&limit=10") { pbHeaders(role = "ADMIN", clientId = "fraud-alerts") }
        val approve = client.patch("/api/v1/fraud/alerts/1/approve") { pbHeaders(role = "ADMIN", clientId = "fraud-approve") }
        val realPrice = client.get("/api/v1/products/1/real-price") { pbHeaders(clientId = "real-price") }
        val lowestEver = client.get("/api/v1/analytics/products/1/lowest-ever") { pbHeaders(clientId = "analytics-lowest") }
        val unitPrice = client.get("/api/v1/analytics/products/1/unit-price") { pbHeaders(clientId = "analytics-unit") }
        val productUsed = client.get("/api/v1/used-market/products/1/price") { pbHeaders(clientId = "used-product") }
        val categoryUsed = client.get("/api/v1/used-market/categories/2/prices?page=1&limit=10") { pbHeaders(clientId = "used-category") }
        val buildEstimate =
            client.post("/api/v1/used-market/pc-builds/1/estimate") {
                pbHeaders(role = "USER", clientId = "used-build-estimate")
                header("X-User-Id", "4")
            }

        assertEquals(HttpStatusCode.OK, alerts.status)
        assertEquals(HttpStatusCode.OK, approve.status)
        assertEquals(HttpStatusCode.OK, realPrice.status)
        assertEquals(HttpStatusCode.OK, lowestEver.status)
        assertEquals(HttpStatusCode.OK, unitPrice.status)
        assertEquals(HttpStatusCode.OK, productUsed.status)
        assertEquals(HttpStatusCode.OK, categoryUsed.status)
        assertEquals(HttpStatusCode.OK, buildEstimate.status)
        assertTrue(alerts.bodyAsText().contains("\"detectedPrice\""))
        assertTrue(approve.bodyAsText().contains("승인"))
        assertTrue(realPrice.bodyAsText().contains("\"totalPrice\""))
        assertTrue(lowestEver.bodyAsText().contains("\"isLowestEver\""))
        assertTrue(unitPrice.bodyAsText().contains("\"unitPrice\""))
        assertTrue(productUsed.bodyAsText().contains("\"averagePrice\""))
        assertTrue(categoryUsed.bodyAsText().contains("\"trend\""))
        assertTrue(buildEstimate.bodyAsText().contains("\"partBreakdown\""))
    }

    @Test
    fun auto_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val models = client.get("/api/v1/auto/models?brand=Hyundai&type=EV") { pbHeaders(clientId = "auto-models") }
        val trims = client.get("/api/v1/auto/models/1/trims") { pbHeaders(clientId = "auto-trims") }
        val estimate =
            client.post("/api/v1/auto/estimate") {
                pbHeaders(clientId = "auto-estimate")
                header("Content-Type", "application/json")
                setBody("""{"modelId":1,"trimId":1,"optionIds":[1]}""")
            }
        val offers = client.get("/api/v1/auto/models/1/lease-offers") { pbHeaders(clientId = "auto-offers") }

        assertEquals(HttpStatusCode.OK, models.status)
        assertEquals(HttpStatusCode.OK, trims.status)
        assertEquals(HttpStatusCode.Created, estimate.status)
        assertEquals(HttpStatusCode.OK, offers.status)
        assertTrue(models.bodyAsText().contains("IONIQ 6"))
        assertTrue(trims.bodyAsText().contains("\"options\""))
        assertTrue(estimate.bodyAsText().contains("\"monthlyPayment\""))
        assertTrue(offers.bodyAsText().contains("\"company\""))
    }

    @Test
    fun auction_and_compare_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val auctionList = client.get("/api/v1/auctions?page=1&limit=10") { pbHeaders(clientId = "auction-list") }
        val createAuction =
            client.post("/api/v1/auctions") {
                pbHeaders(role = "USER", clientId = "auction-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"title":"모니터 구매 요청","description":"32인치 QHD 모니터를 찾고 있습니다.","categoryId":2,"budget":500000}""")
            }
        val createdAuctionId =
            Json.parseToJsonElement(createAuction.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val createBid =
            client.post("/api/v1/auctions/$createdAuctionId/bids") {
                pbHeaders(role = "SELLER", clientId = "auction-bid-create")
                header("X-User-Id", "2")
                header("Content-Type", "application/json")
                setBody("""{"price":470000,"description":"이틀 내 발송","deliveryDays":2}""")
            }
        val createdBidId =
            Json.parseToJsonElement(createBid.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val updateBid =
            client.patch("/api/v1/auctions/$createdAuctionId/bids/$createdBidId") {
                pbHeaders(role = "SELLER", clientId = "auction-bid-update")
                header("X-User-Id", "2")
                header("Content-Type", "application/json")
                setBody("""{"price":465000,"deliveryDays":1}""")
            }
        val auctionDetail = client.get("/api/v1/auctions/$createdAuctionId") { pbHeaders(clientId = "auction-detail") }
        val selectBid =
            client.patch("/api/v1/auctions/$createdAuctionId/bids/$createdBidId/select") {
                pbHeaders(role = "USER", clientId = "auction-select")
                header("X-User-Id", "4")
            }

        val createAuctionToCancel =
            client.post("/api/v1/auctions") {
                pbHeaders(role = "USER", clientId = "auction-create-cancel")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"title":"취소할 경매","description":"테스트용 경매"}""")
            }
        val cancelAuctionId =
            Json.parseToJsonElement(createAuctionToCancel.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val cancelAuction =
            client.delete("/api/v1/auctions/$cancelAuctionId") {
                pbHeaders(role = "USER", clientId = "auction-cancel")
                header("X-User-Id", "4")
            }

        val compareAdd =
            client.post("/api/v1/compare/add") {
                pbHeaders(clientId = "compare-add")
                header("X-Compare-Key", "test-compare")
                header("Content-Type", "application/json")
                setBody("""{"productId":3}""")
            }
        val compareList =
            client.get("/api/v1/compare") {
                pbHeaders(clientId = "compare-list")
                header("X-Compare-Key", "test-compare")
            }
        val compareDetail =
            client.get("/api/v1/compare/detail") {
                pbHeaders(clientId = "compare-detail")
                header("X-Compare-Key", "test-compare")
            }
        val compareRemove =
            client.delete("/api/v1/compare/3") {
                pbHeaders(clientId = "compare-remove")
                header("X-Compare-Key", "test-compare")
            }

        assertEquals(HttpStatusCode.OK, auctionList.status)
        assertEquals(HttpStatusCode.Created, createAuction.status)
        assertEquals(HttpStatusCode.Created, createBid.status)
        assertEquals(HttpStatusCode.OK, updateBid.status)
        assertEquals(HttpStatusCode.OK, auctionDetail.status)
        assertEquals(HttpStatusCode.OK, selectBid.status)
        assertEquals(HttpStatusCode.Created, createAuctionToCancel.status)
        assertEquals(HttpStatusCode.OK, cancelAuction.status)
        assertEquals(HttpStatusCode.OK, compareAdd.status)
        assertEquals(HttpStatusCode.OK, compareList.status)
        assertEquals(HttpStatusCode.OK, compareDetail.status)
        assertEquals(HttpStatusCode.OK, compareRemove.status)
        assertTrue(auctionDetail.bodyAsText().contains("\"bids\""))
        assertTrue(updateBid.bodyAsText().contains("465000"))
        assertTrue(selectBid.bodyAsText().contains("낙찰"))
        assertTrue(compareList.bodyAsText().contains("\"compareList\""))
        assertTrue(compareDetail.bodyAsText().contains("\"diff\""))
    }
}
