package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun observabilityOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/admin/observability/metrics", "Observability", "Observability metrics", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("totalRequests" to 1520, "errorRate" to 0.01, "avgLatencyMs" to 42))
        },
        endpoint(HttpMethod.Get, "/admin/observability/traces", "Observability", "Observability traces", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("requestId" to "req-1", "method" to "GET", "path" to "/api/v1/products", "statusCode" to 200, "durationMs" to 24))))
        },
        endpoint(HttpMethod.Get, "/admin/observability/dashboard", "Observability", "Observability dashboard", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("process" to mapOf("uptimeSeconds" to 3600), "metrics" to mapOf("totalRequests" to 1520), "opsSummary" to mapOf("overallStatus" to "healthy")))
        },
    )
