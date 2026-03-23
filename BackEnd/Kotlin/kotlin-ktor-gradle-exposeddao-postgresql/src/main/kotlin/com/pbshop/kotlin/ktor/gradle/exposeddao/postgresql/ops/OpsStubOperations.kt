package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ops

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun opsOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/fraud/alerts", "Fraud", "Fraud alerts", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "OPEN", "productId" to 1))) },
        endpoint(HttpMethod.Patch, "/fraud/alerts/{id}/approve", "Fraud", "Approve fraud alert", roles = setOf(PbRole.ADMIN)) { message("Fraud alert approved.") },
        endpoint(HttpMethod.Patch, "/fraud/alerts/{id}/reject", "Fraud", "Reject fraud alert", roles = setOf(PbRole.ADMIN)) { message("Fraud alert rejected.") },
        endpoint(HttpMethod.Get, "/products/{id}/real-price", "Fraud", "Real price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "productPrice" to 1500000, "shippingFee" to 3000, "totalPrice" to 1503000))
        },
        endpoint(HttpMethod.Get, "/analytics/products/{id}/lowest-ever", "Analytics", "Lowest ever price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "isLowestEver" to true, "lowestPrice" to 1490000))
        },
        endpoint(HttpMethod.Get, "/analytics/products/{id}/unit-price", "Analytics", "Unit price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "unitPrice" to 12450, "unit" to "GB"))
        },
        endpoint(HttpMethod.Get, "/used-market/products/{id}/price", "Used Market", "Used market product price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "averagePrice" to 980000, "trend" to "STABLE"))
        },
        endpoint(HttpMethod.Get, "/used-market/categories/{id}/prices", "Used Market", "Used market category prices") { paged(listOf(mapOf("categoryId" to 1, "averagePrice" to 540000))) },
        endpoint(HttpMethod.Post, "/used-market/pc-builds/{buildId}/estimate", "Used Market", "Used PC build estimate", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("buildId" to call.pathParam("buildId", "1"), "estimatedPrice" to 1200000))
        },
        endpoint(HttpMethod.Get, "/auto/models", "Auto", "Auto models") { StubResponse(data = listOf(mapOf("id" to 1, "brand" to "PB Motors", "name" to "Electro X"))) },
        endpoint(HttpMethod.Get, "/auto/models/{id}/trims", "Auto", "Auto trims") { call -> StubResponse(data = listOf(mapOf("modelId" to call.pathParam("id", "1"), "name" to "Long Range"))) },
        endpoint(HttpMethod.Post, "/auto/estimate", "Auto", "Auto estimate") { StubResponse(data = mapOf("basePrice" to 48000000, "optionPrice" to 3500000, "totalPrice" to 53200000)) },
        endpoint(HttpMethod.Get, "/auto/models/{id}/lease-offers", "Auto", "Auto lease offers") { call ->
            StubResponse(data = listOf(mapOf("modelId" to call.pathParam("id", "1"), "provider" to "PB Lease", "monthlyPayment" to 450000)))
        },
        endpoint(HttpMethod.Post, "/auctions", "Auction", "Create reverse auction", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "OPEN")) },
        endpoint(HttpMethod.Get, "/auctions", "Auction", "Auction list") { paged(listOf(mapOf("id" to 1, "status" to "OPEN", "title" to "Laptop bulk purchase"))) },
        endpoint(HttpMethod.Get, "/auctions/{id}", "Auction", "Auction detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "OPEN", "bids" to listOf(mapOf("id" to 1, "price" to 1490000))))
        },
        endpoint(HttpMethod.Post, "/auctions/{id}/bids", "Auction", "Create bid", roles = setOf(PbRole.SELLER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "price" to 1490000)) },
        endpoint(HttpMethod.Patch, "/auctions/{id}/bids/{bidId}/select", "Auction", "Select bid", roles = setOf(PbRole.USER)) { message("Bid selected.") },
        endpoint(HttpMethod.Delete, "/auctions/{id}", "Auction", "Delete auction", roles = setOf(PbRole.USER)) { message("Auction deleted.") },
        endpoint(HttpMethod.Patch, "/auctions/{id}/bids/{bidId}", "Auction", "Update bid", roles = setOf(PbRole.SELLER)) { call ->
            StubResponse(data = mapOf("bidId" to call.pathParam("bidId", "1"), "price" to 1480000))
        },
        endpoint(HttpMethod.Delete, "/auctions/{id}/bids/{bidId}", "Auction", "Delete bid", roles = setOf(PbRole.SELLER)) { message("Bid deleted.") },
        endpoint(HttpMethod.Post, "/compare/add", "Compare", "Add compare item") { StubResponse(data = mapOf("compareList" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")))) },
        endpoint(HttpMethod.Delete, "/compare/{productId}", "Compare", "Delete compare item") { StubResponse(data = mapOf("compareList" to emptyList<String>())) },
        endpoint(HttpMethod.Get, "/compare", "Compare", "Compare list") { StubResponse(data = mapOf("compareList" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")))) },
        endpoint(HttpMethod.Get, "/compare/detail", "Compare", "Compare detail") {
            StubResponse(data = mapOf("items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), "highlights" to listOf("weight", "battery")))
        },
        endpoint(HttpMethod.Get, "/admin/settings/extensions", "Admin Settings", "Upload extension settings", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("extensions" to listOf("jpg", "png", "webp", "mp4"))) },
        endpoint(HttpMethod.Post, "/admin/settings/extensions", "Admin Settings", "Update upload extension settings", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("extensions" to listOf("jpg", "png", "webp", "mp4", "mp3"))) },
        endpoint(HttpMethod.Get, "/admin/settings/upload-limits", "Admin Settings", "Upload limits", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("image" to 5, "video" to 100, "audio" to 20)) },
        endpoint(HttpMethod.Patch, "/admin/settings/upload-limits", "Admin Settings", "Update upload limits", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("image" to 8, "video" to 120, "audio" to 20)) },
        endpoint(HttpMethod.Get, "/admin/settings/review-policy", "Admin Settings", "Review policy", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("maxImageCount" to 10, "pointAmount" to 500)) },
        endpoint(HttpMethod.Patch, "/admin/settings/review-policy", "Admin Settings", "Update review policy", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("maxImageCount" to 12, "pointAmount" to 700)) },
        endpoint(HttpMethod.Get, "/resilience/circuit-breakers", "Resilience", "Circuit breakers", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("name" to "payment-provider", "state" to "CLOSED"))))
        },
        endpoint(HttpMethod.Get, "/resilience/circuit-breakers/policies", "Resilience", "Circuit breaker policies", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("name" to "payment-provider", "options" to mapOf("failureThreshold" to 5), "stats" to mapOf("successCount" to 120)))))
        },
        endpoint(HttpMethod.Get, "/resilience/circuit-breakers/{name}", "Resilience", "Circuit breaker detail", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("name" to call.pathParam("name", "payment-provider"), "state" to "CLOSED"))
        },
        endpoint(HttpMethod.Post, "/resilience/circuit-breakers/{name}/reset", "Resilience", "Reset circuit breaker", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("message" to "Circuit breaker reset.", "name" to call.pathParam("name", "payment-provider")))
        },
        endpoint(HttpMethod.Get, "/errors/codes", "Error Code", "Error code catalog") { StubResponse(data = mapOf("total" to 4, "items" to listOf(mapOf("code" to "PRODUCT_NOT_FOUND", "message" to "Product not found")))) },
        endpoint(HttpMethod.Get, "/errors/codes/{key}", "Error Code", "Error code detail") { call ->
            StubResponse(data = mapOf("code" to call.pathParam("key", "PRODUCT_NOT_FOUND"), "message" to "Product not found"))
        },
        endpoint(HttpMethod.Get, "/admin/queues/supported", "Queue Admin", "Supported queues", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("items" to listOf("crawler", "image-processing", "search-index"))) },
        endpoint(HttpMethod.Get, "/admin/queues/stats", "Queue Admin", "Queue stats", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("total" to 3, "items" to listOf(mapOf("queueName" to "crawler", "paused" to false, "counts" to mapOf("failed" to 1, "waiting" to 2)))))
        },
        endpoint(HttpMethod.Post, "/admin/queues/auto-retry", "Queue Admin", "Auto retry failed jobs", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("retriedTotal" to 1, "items" to listOf(mapOf("queueName" to "crawler", "requeuedCount" to 1))))
        },
        endpoint(HttpMethod.Get, "/admin/queues/{queueName}/failed", "Queue Admin", "Failed queue jobs", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("jobId" to "job-1", "status" to "failed", "attempts" to 3))) },
        endpoint(HttpMethod.Post, "/admin/queues/{queueName}/failed/retry", "Queue Admin", "Retry failed queue jobs", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("requested" to 1, "requeuedCount" to 1, "jobIds" to listOf("job-1")))
        },
        endpoint(HttpMethod.Post, "/admin/queues/{queueName}/jobs/{jobId}/retry", "Queue Admin", "Retry single queue job", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("retried" to true)) },
        endpoint(HttpMethod.Delete, "/admin/queues/{queueName}/jobs/{jobId}", "Queue Admin", "Delete queue job", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("removed" to true)) },
        endpoint(HttpMethod.Get, "/admin/ops-dashboard/summary", "Ops Dashboard", "Ops dashboard summary", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("checkedAt" to "2026-03-23T10:00:00Z", "overallStatus" to "healthy", "health" to mapOf("db" to "UP", "redis" to "NOT_CONFIGURED", "elasticsearch" to "NOT_CONFIGURED"), "queue" to mapOf("failed" to 1), "alerts" to listOf<String>(), "alertCount" to 0))
        },
        endpoint(HttpMethod.Get, "/admin/observability/metrics", "Observability", "Observability metrics", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("totalRequests" to 1520, "errorRate" to 0.01, "avgLatencyMs" to 42)) },
        endpoint(HttpMethod.Get, "/admin/observability/traces", "Observability", "Observability traces", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("requestId" to "req-1", "method" to "GET", "path" to "/api/v1/products", "statusCode" to 200, "durationMs" to 24))))
        },
        endpoint(HttpMethod.Get, "/admin/observability/dashboard", "Observability", "Observability dashboard", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("process" to mapOf("uptimeSeconds" to 3600), "metrics" to mapOf("totalRequests" to 1520), "opsSummary" to mapOf("overallStatus" to "healthy")))
        },
        endpoint(HttpMethod.Get, "/query/products", "Query", "Query product read model") { paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"))) },
        endpoint(HttpMethod.Get, "/query/products/{productId}", "Query", "Query product read model detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("productId", "1"), "name" to "PB GalaxyBook 4 Pro", "source" to "query-model"))
        },
        endpoint(HttpMethod.Post, "/admin/query/products/{productId}/sync", "Query", "Sync single query product", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("productId" to call.pathParam("productId", "1"), "queued" to true))
        },
        endpoint(HttpMethod.Post, "/admin/query/products/rebuild", "Query", "Rebuild query read model", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("queued" to true, "scope" to "all-products")) },
        endpoint(HttpMethod.Post, "/products/{id}/images", "Image", "Attach product image", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("productId" to call.pathParam("id", "1"), "imageId" to 1))
        },
        endpoint(HttpMethod.Delete, "/products/{id}/images/{imageId}", "Image", "Delete product image", roles = setOf(PbRole.ADMIN)) { message("Product image deleted.") },
        endpoint(HttpMethod.Post, "/users/me/profile-image", "Image", "Upload profile image", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("profileImageUrl" to "/uploads/profile/user-1.webp")) },
        endpoint(HttpMethod.Delete, "/users/me/profile-image", "Image", "Delete profile image", roles = setOf(PbRole.USER)) { message("Profile image deleted.") },
    )
