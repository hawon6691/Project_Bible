package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.discovery

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun discoveryOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/rankings/products/popular", "Ranking", "Popular product ranking") {
            StubResponse(data = listOf(mapOf("rank" to 1, "product" to productSummary(1, "PB GalaxyBook 4 Pro"), "score" to 15230)))
        },
        endpoint(HttpMethod.Get, "/rankings/searches", "Ranking", "Popular search ranking") {
            StubResponse(data = listOf(mapOf("rank" to 1, "keyword" to "galaxybook", "searchCount" to 5230)))
        },
        endpoint(HttpMethod.Get, "/recommendations/today", "Recommendation", "Today recommendations") {
            StubResponse(data = listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")))
        },
        endpoint(HttpMethod.Get, "/recommendations/personalized", "Recommendation", "Personalized recommendations", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(productSummary(3, "PB Gaming Laptop 17")))
        },
        endpoint(HttpMethod.Get, "/admin/recommendations", "Recommendation", "Admin recommendation list", roles = setOf(PbRole.ADMIN)) { StubResponse(data = listOf(mapOf("id" to 1, "productId" to 1, "slot" to "today"))) },
        endpoint(HttpMethod.Post, "/admin/recommendations", "Recommendation", "Create recommendation", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "productId" to 2, "slot" to "today")) },
        endpoint(HttpMethod.Delete, "/admin/recommendations/{id}", "Recommendation", "Delete recommendation", roles = setOf(PbRole.ADMIN)) { message("Recommendation deleted.") },
        endpoint(HttpMethod.Get, "/deals", "Deal", "Deal list") { paged(listOf(mapOf("id" to 1, "title" to "Spring laptop sale", "discountRate" to 15))) },
        endpoint(HttpMethod.Get, "/deals/{id}", "Deal", "Deal detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Spring laptop sale", "products" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Post, "/deals", "Deal", "Create deal", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New deal")) },
        endpoint(HttpMethod.Patch, "/deals/{id}", "Deal", "Update deal", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated deal")) },
        endpoint(HttpMethod.Delete, "/deals/{id}", "Deal", "Delete deal", roles = setOf(PbRole.ADMIN)) { message("Deal deleted.") },
        endpoint(HttpMethod.Get, "/search", "Search", "Search catalog") {
            StubResponse(data = mapOf("hits" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")), "facets" to mapOf("categories" to listOf(mapOf("id" to 1, "name" to "Laptop", "count" to 12))), "suggestions" to listOf("galaxybook", "galaxybook 4"), "totalCount" to 1), meta = mapOf("page" to 1, "limit" to 20, "totalCount" to 1, "totalPages" to 1))
        },
        endpoint(HttpMethod.Get, "/search/autocomplete", "Search", "Search autocomplete") {
            StubResponse(data = mapOf("keywords" to listOf("galaxybook 4 pro"), "products" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")), "categories" to listOf(mapOf("id" to 1, "name" to "Laptop"))))
        },
        endpoint(HttpMethod.Get, "/search/popular", "Search", "Popular search keywords") { StubResponse(data = listOf(mapOf("keyword" to "galaxybook", "count" to 5230))) },
        endpoint(HttpMethod.Post, "/search/recent", "Search", "Save recent search", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "keyword" to "galaxybook", "createdAt" to "2026-03-23T10:00:00Z")) },
        endpoint(HttpMethod.Get, "/search/recent", "Search", "Recent searches", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "keyword" to "galaxybook"), mapOf("id" to 2, "keyword" to "7800x3d"))) },
        endpoint(HttpMethod.Delete, "/search/recent/{id}", "Search", "Delete recent search", roles = setOf(PbRole.USER)) { message("Recent search deleted.") },
        endpoint(HttpMethod.Delete, "/search/recent", "Search", "Delete all recent searches", roles = setOf(PbRole.USER)) { message("Recent search history cleared.") },
        endpoint(HttpMethod.Patch, "/search/preferences", "Search", "Update search preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("saveRecentSearches" to true)) },
        endpoint(HttpMethod.Get, "/search/admin/weights", "Search", "Search weights", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("nameWeight" to 1.0, "keywordWeight" to 1.5, "clickWeight" to 0.8)) },
        endpoint(HttpMethod.Patch, "/search/admin/weights", "Search", "Update search weights", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("nameWeight" to 1.2, "keywordWeight" to 1.4, "clickWeight" to 0.9)) },
        endpoint(HttpMethod.Get, "/search/admin/index/status", "Search", "Search index status", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("status" to "HEALTHY", "documents" to 1200, "lastIndexedAt" to "2026-03-23T09:55:00Z")) },
        endpoint(HttpMethod.Post, "/search/admin/index/reindex", "Search", "Reindex search", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "Reindex queued.", "queued" to true)) },
        endpoint(HttpMethod.Post, "/search/admin/index/products/{id}/reindex", "Search", "Reindex product", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "Product reindex queued.", "productId" to call.pathParam("id", "1"))) },
        endpoint(HttpMethod.Get, "/search/admin/index/outbox/summary", "Search", "Search outbox summary", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("pending" to 2, "failed" to 1, "lastProcessedAt" to "2026-03-23T09:58:00Z")) },
        endpoint(HttpMethod.Post, "/search/admin/index/outbox/requeue-failed", "Search", "Requeue failed outbox entries", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("requeuedCount" to 1)) },
        endpoint(HttpMethod.Post, "/crawler/admin/jobs", "Crawler", "Create crawler job", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "name" to "Coupang laptop crawler", "status" to "IDLE")) },
        endpoint(HttpMethod.Get, "/crawler/admin/jobs", "Crawler", "Crawler job list", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "name" to "Coupang laptop crawler", "status" to "IDLE"))) },
        endpoint(HttpMethod.Patch, "/crawler/admin/jobs/{id}", "Crawler", "Update crawler job", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "UPDATED")) },
        endpoint(HttpMethod.Delete, "/crawler/admin/jobs/{id}", "Crawler", "Delete crawler job", roles = setOf(PbRole.ADMIN)) { message("Crawler job deleted.") },
        endpoint(HttpMethod.Post, "/crawler/admin/jobs/{id}/run", "Crawler", "Run crawler job", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("message" to "Crawler job queued.", "runId" to "run-${call.pathParam("id", "1")}")) },
        endpoint(HttpMethod.Post, "/crawler/admin/triggers", "Crawler", "Trigger crawler", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("message" to "Crawler trigger queued.", "runId" to "run-trigger-1")) },
        endpoint(HttpMethod.Get, "/crawler/admin/runs", "Crawler", "Crawler runs", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "SUCCESS", "itemsProcessed" to 245))) },
        endpoint(HttpMethod.Get, "/crawler/admin/monitoring", "Crawler", "Crawler monitoring summary", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("queueDepth" to 2, "failedRuns" to 1, "lastSuccessAt" to "2026-03-23T09:55:00Z")) },
        endpoint(HttpMethod.Get, "/predictions/products/{productId}/price-trend", "Prediction", "Price trend prediction") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("productId", "1"), "trend" to "FALLING", "recommendation" to "BUY_SOON", "predictions" to listOf(mapOf("date" to "2026-03-24", "predictedPrice" to 1585000, "confidence" to 0.85))))
        },
        endpoint(HttpMethod.Get, "/sellers/{id}/trust", "Trust", "Seller trust detail") { call -> StubResponse(data = mapOf("sellerId" to call.pathParam("id", "1"), "overallScore" to 95, "grade" to "A+")) },
        endpoint(HttpMethod.Get, "/sellers/{id}/reviews", "Trust", "Seller reviews") { paged(listOf(mapOf("id" to 1, "rating" to 5, "content" to "Fast delivery"))) },
        endpoint(HttpMethod.Post, "/sellers/{id}/reviews", "Trust", "Create seller review", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "rating" to 5)) },
        endpoint(HttpMethod.Patch, "/seller-reviews/{id}", "Trust", "Update seller review", roles = setOf(PbRole.USER)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "rating" to 4)) },
        endpoint(HttpMethod.Delete, "/seller-reviews/{id}", "Trust", "Delete seller review", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Seller review deleted.") },
        endpoint(HttpMethod.Get, "/i18n/translations", "I18n", "Translations") { StubResponse(data = listOf(mapOf("id" to 1, "key" to "product.lowest_price", "value" to "Lowest Price", "locale" to "en"))) },
        endpoint(HttpMethod.Post, "/admin/i18n/translations", "I18n", "Upsert translation", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "key" to "product.add_to_cart", "locale" to "en")) },
        endpoint(HttpMethod.Delete, "/admin/i18n/translations/{id}", "I18n", "Delete translation", roles = setOf(PbRole.ADMIN)) { message("Translation deleted.") },
        endpoint(HttpMethod.Get, "/i18n/exchange-rates", "I18n", "Exchange rates") { StubResponse(data = listOf(mapOf("baseCurrency" to "KRW", "targetCurrency" to "USD", "rate" to 0.000748))) },
        endpoint(HttpMethod.Get, "/i18n/convert", "I18n", "Currency conversion") { StubResponse(data = mapOf("originalAmount" to 1590000, "originalCurrency" to "KRW", "convertedAmount" to 1189.32, "targetCurrency" to "USD")) },
    )
