package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun resilienceOperations(): List<StubOperation> =
    listOf(
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
    )
