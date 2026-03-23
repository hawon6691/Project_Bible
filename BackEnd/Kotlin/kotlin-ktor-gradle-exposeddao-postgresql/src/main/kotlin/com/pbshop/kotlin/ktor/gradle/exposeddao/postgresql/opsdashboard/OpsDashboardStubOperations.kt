package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun opsDashboardOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/admin/ops-dashboard/summary", "Ops Dashboard", "Ops dashboard summary", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("checkedAt" to "2026-03-23T10:00:00Z", "overallStatus" to "healthy", "health" to mapOf("db" to "UP", "redis" to "NOT_CONFIGURED", "elasticsearch" to "NOT_CONFIGURED"), "queue" to mapOf("failed" to 1), "alerts" to emptyList<String>(), "alertCount" to 0))
        },
    )
