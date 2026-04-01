package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpsAdminActualApiTest {
    @Test
    fun admin_settings_error_code_and_query_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val extensions = client.get("/api/v1/admin/settings/extensions") { pbHeaders(role = "ADMIN", clientId = "ops-ext") }
        val updateExtensions =
            client.post("/api/v1/admin/settings/extensions") {
                pbHeaders(role = "ADMIN", clientId = "ops-ext-update")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"extensions":["PNG","jpg","pdf"]}""")
            }
        val uploadLimits = client.get("/api/v1/admin/settings/upload-limits") { pbHeaders(role = "ADMIN", clientId = "ops-upload-limits") }
        val patchUploadLimits =
            client.patch("/api/v1/admin/settings/upload-limits") {
                pbHeaders(role = "ADMIN", clientId = "ops-upload-limits-patch")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"image":8,"video":120}""")
            }
        val reviewPolicy = client.get("/api/v1/admin/settings/review-policy") { pbHeaders(role = "ADMIN", clientId = "ops-review-policy") }
        val patchReviewPolicy =
            client.patch("/api/v1/admin/settings/review-policy") {
                pbHeaders(role = "ADMIN", clientId = "ops-review-policy-patch")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"maxImageCount":12,"pointAmount":700}""")
            }
        val errorCodes = client.get("/api/v1/errors/codes") { pbHeaders(clientId = "ops-errors-list") }
        val errorCodeDetail = client.get("/api/v1/errors/codes/AUTH_REQUIRED") { pbHeaders(clientId = "ops-errors-detail") }
        val missingErrorCode = client.get("/api/v1/errors/codes/DOES_NOT_EXIST") { pbHeaders(clientId = "ops-errors-missing") }
        val queryList = client.get("/api/v1/query/products?categoryId=1&keyword=PB&sort=popularity") { pbHeaders(clientId = "ops-query-list") }
        val queryDetail = client.get("/api/v1/query/products/1") { pbHeaders(clientId = "ops-query-detail") }
        val querySync =
            client.post("/api/v1/admin/query/products/1/sync") {
                pbHeaders(role = "ADMIN", clientId = "ops-query-sync")
            }
        val queryRebuild =
            client.post("/api/v1/admin/query/products/rebuild") {
                pbHeaders(role = "ADMIN", clientId = "ops-query-rebuild")
            }

        assertEquals(HttpStatusCode.OK, extensions.status)
        assertEquals(HttpStatusCode.OK, updateExtensions.status)
        assertEquals(HttpStatusCode.OK, uploadLimits.status)
        assertEquals(HttpStatusCode.OK, patchUploadLimits.status)
        assertEquals(HttpStatusCode.OK, reviewPolicy.status)
        assertEquals(HttpStatusCode.OK, patchReviewPolicy.status)
        assertEquals(HttpStatusCode.OK, errorCodes.status)
        assertEquals(HttpStatusCode.OK, errorCodeDetail.status)
        assertEquals(HttpStatusCode.OK, missingErrorCode.status)
        assertEquals(HttpStatusCode.OK, queryList.status)
        assertEquals(HttpStatusCode.OK, queryDetail.status)
        assertEquals(HttpStatusCode.Created, querySync.status)
        assertEquals(HttpStatusCode.Created, queryRebuild.status)

        assertTrue(updateExtensions.bodyAsText().lowercase().contains("png"))
        assertTrue(patchUploadLimits.bodyAsText().isNotBlank())
        assertTrue(patchReviewPolicy.bodyAsText().isNotBlank())
        assertTrue(errorCodes.bodyAsText().contains("AUTH_REQUIRED"))
        assertTrue(errorCodeDetail.bodyAsText().contains("AUTH_REQUIRED"))
        assertTrue(missingErrorCode.bodyAsText().replace(" ", "").contains("\"data\":null"))
        assertTrue(queryList.bodyAsText().contains("popularityScore"))
        assertTrue(queryDetail.bodyAsText().contains("\"productId\""))
        assertTrue(queryRebuild.bodyAsText().contains("syncedCount"))
    }

    @Test
    fun queue_admin_and_resilience_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val circuits = client.get("/api/v1/resilience/circuit-breakers") { pbHeaders(role = "ADMIN", clientId = "ops-circuits") }
        val policies = client.get("/api/v1/resilience/circuit-breakers/policies") { pbHeaders(role = "ADMIN", clientId = "ops-policies") }
        val circuitDetail = client.get("/api/v1/resilience/circuit-breakers/payment-provider") { pbHeaders(role = "ADMIN", clientId = "ops-circuit-detail") }
        val circuitReset =
            client.post("/api/v1/resilience/circuit-breakers/payment-provider/reset") {
                pbHeaders(role = "ADMIN", clientId = "ops-circuit-reset")
            }
        val supportedQueues = client.get("/api/v1/admin/queues/supported") { pbHeaders(role = "ADMIN", clientId = "ops-queues-supported") }
        val queueStats = client.get("/api/v1/admin/queues/stats") { pbHeaders(role = "ADMIN", clientId = "ops-queues-stats") }
        val failedSearch = client.get("/api/v1/admin/queues/search-index/failed?page=1&limit=10") { pbHeaders(role = "ADMIN", clientId = "ops-queues-failed") }
        val retrySingle =
            client.post("/api/v1/admin/queues/search-index/jobs/1/retry") {
                pbHeaders(role = "ADMIN", clientId = "ops-queues-single-retry")
            }
        val autoRetry =
            client.post("/api/v1/admin/queues/auto-retry?perQueueLimit=1&maxTotal=2") {
                pbHeaders(role = "ADMIN", clientId = "ops-queues-auto-retry")
            }
        val removeCrawler =
            client.delete("/api/v1/admin/queues/crawler/jobs/11") {
                pbHeaders(role = "ADMIN", clientId = "ops-queues-remove")
            }

        assertEquals(HttpStatusCode.OK, circuits.status)
        assertEquals(HttpStatusCode.OK, policies.status)
        assertEquals(HttpStatusCode.OK, circuitDetail.status)
        assertEquals(HttpStatusCode.OK, circuitReset.status)
        assertEquals(HttpStatusCode.OK, supportedQueues.status)
        assertEquals(HttpStatusCode.OK, queueStats.status)
        assertEquals(HttpStatusCode.OK, failedSearch.status)
        assertEquals(HttpStatusCode.OK, retrySingle.status)
        assertEquals(HttpStatusCode.OK, autoRetry.status)
        assertEquals(HttpStatusCode.OK, removeCrawler.status)

        assertTrue(circuits.bodyAsText().contains("\"payment-provider\""))
        assertTrue(policies.bodyAsText().contains("\"failureThreshold\""))
        assertTrue(circuitReset.bodyAsText().contains("\"status\": \"CLOSED\""))
        assertTrue(supportedQueues.bodyAsText().contains("\"search-index\""))
        assertTrue(queueStats.bodyAsText().contains("\"video-transcode\""))
        assertTrue(failedSearch.bodyAsText().contains("\"failedReason\""))
        assertTrue(retrySingle.bodyAsText().contains("\"retried\": true"))
        assertTrue(autoRetry.bodyAsText().contains("\"retriedTotal\""))
        assertTrue(removeCrawler.bodyAsText().contains("\"removed\": true"))
    }

    @Test
    fun observability_and_ops_dashboard_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        client.get("/api/v1/query/products") { pbHeaders(clientId = "trace-query-products") }
        client.get("/api/v1/errors/codes") { pbHeaders(clientId = "trace-errors-codes") }
        client.get("/api/v1/query/products/1") { pbHeaders(clientId = "trace-query-detail") }

        val metrics = client.get("/api/v1/admin/observability/metrics") { pbHeaders(role = "ADMIN", clientId = "ops-ob-metrics") }
        val traces =
            client.get("/api/v1/admin/observability/traces?limit=10&pathContains=/api/v1/query/products") {
                pbHeaders(role = "ADMIN", clientId = "ops-ob-traces")
            }
        val dashboard = client.get("/api/v1/admin/observability/dashboard") { pbHeaders(role = "ADMIN", clientId = "ops-ob-dashboard") }
        val summary = client.get("/api/v1/admin/ops-dashboard/summary") { pbHeaders(role = "ADMIN", clientId = "ops-dashboard-summary") }

        assertEquals(HttpStatusCode.OK, metrics.status)
        assertEquals(HttpStatusCode.OK, traces.status)
        assertEquals(HttpStatusCode.OK, dashboard.status)
        assertEquals(HttpStatusCode.OK, summary.status)

        assertTrue(metrics.bodyAsText().contains("\"totalRequests\""))
        assertTrue(metrics.bodyAsText().contains("\"statusBuckets\""))
        assertTrue(traces.bodyAsText().contains("/api/v1/query/products"))
        assertTrue(dashboard.bodyAsText().contains("\"process\""))
        assertTrue(dashboard.bodyAsText().contains("\"opsSummary\""))
        assertTrue(summary.bodyAsText().contains("\"overallStatus\""))
        assertTrue(summary.bodyAsText().contains("\"alerts\""))
    }
}
