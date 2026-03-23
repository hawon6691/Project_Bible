package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun queueAdminOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/admin/queues/supported", "Queue Admin", "Supported queues", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf("crawler", "image-processing", "search-index")))
        },
        endpoint(HttpMethod.Get, "/admin/queues/stats", "Queue Admin", "Queue stats", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("total" to 3, "items" to listOf(mapOf("queueName" to "crawler", "paused" to false, "counts" to mapOf("failed" to 1, "waiting" to 2)))))
        },
        endpoint(HttpMethod.Post, "/admin/queues/auto-retry", "Queue Admin", "Auto retry failed jobs", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("retriedTotal" to 1, "items" to listOf(mapOf("queueName" to "crawler", "requeuedCount" to 1))))
        },
        endpoint(HttpMethod.Get, "/admin/queues/{queueName}/failed", "Queue Admin", "Failed queue jobs", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("jobId" to "job-1", "status" to "failed", "attempts" to 3)))
        },
        endpoint(HttpMethod.Post, "/admin/queues/{queueName}/failed/retry", "Queue Admin", "Retry failed queue jobs", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("requested" to 1, "requeuedCount" to 1, "jobIds" to listOf("job-1")))
        },
        endpoint(HttpMethod.Post, "/admin/queues/{queueName}/jobs/{jobId}/retry", "Queue Admin", "Retry single queue job", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("retried" to true))
        },
        endpoint(HttpMethod.Delete, "/admin/queues/{queueName}/jobs/{jobId}", "Queue Admin", "Delete queue job", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("removed" to true))
        },
    )
