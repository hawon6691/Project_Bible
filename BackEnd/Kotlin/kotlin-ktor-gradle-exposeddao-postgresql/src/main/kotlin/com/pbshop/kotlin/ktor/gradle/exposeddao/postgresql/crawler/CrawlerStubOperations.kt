package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun crawlerOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/crawler/admin/jobs", "Crawler", "Create crawler job", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "name" to "Coupang laptop crawler", "status" to "IDLE"))
        },
        endpoint(HttpMethod.Get, "/crawler/admin/jobs", "Crawler", "Crawler job list", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "name" to "Coupang laptop crawler", "status" to "IDLE")))
        },
        endpoint(HttpMethod.Patch, "/crawler/admin/jobs/{id}", "Crawler", "Update crawler job", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "UPDATED"))
        },
        endpoint(HttpMethod.Delete, "/crawler/admin/jobs/{id}", "Crawler", "Delete crawler job", roles = setOf(PbRole.ADMIN)) {
            message("Crawler job deleted.")
        },
        endpoint(HttpMethod.Post, "/crawler/admin/jobs/{id}/run", "Crawler", "Run crawler job", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("message" to "Crawler job queued.", "runId" to "run-${call.pathParam("id", "1")}"))
        },
        endpoint(HttpMethod.Post, "/crawler/admin/triggers", "Crawler", "Trigger crawler", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("message" to "Crawler trigger queued.", "runId" to "run-trigger-1"))
        },
        endpoint(HttpMethod.Get, "/crawler/admin/runs", "Crawler", "Crawler runs", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "status" to "SUCCESS", "itemsProcessed" to 245)))
        },
        endpoint(HttpMethod.Get, "/crawler/admin/monitoring", "Crawler", "Crawler monitoring summary", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("queueDepth" to 2, "failedRuns" to 1, "lastSuccessAt" to "2026-03-23T09:55:00Z"))
        },
    )
