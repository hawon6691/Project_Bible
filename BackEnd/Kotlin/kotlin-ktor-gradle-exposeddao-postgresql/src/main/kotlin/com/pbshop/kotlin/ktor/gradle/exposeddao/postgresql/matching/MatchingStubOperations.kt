package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun matchingOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/matching/pending", "Matching", "Pending product mappings", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "sourceName" to "Vendor product", "status" to "PENDING")))
        },
        endpoint(HttpMethod.Patch, "/matching/{id}/approve", "Matching", "Approve product mapping", roles = setOf(PbRole.ADMIN)) {
            message("Product mapping approved.")
        },
        endpoint(HttpMethod.Patch, "/matching/{id}/reject", "Matching", "Reject product mapping", roles = setOf(PbRole.ADMIN)) {
            message("Product mapping rejected.")
        },
        endpoint(HttpMethod.Post, "/matching/auto-match", "Matching", "Run auto match", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("matchedCount" to 12, "pendingCount" to 3))
        },
        endpoint(HttpMethod.Get, "/matching/stats", "Matching", "Product mapping stats", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("approved" to 120, "pending" to 8, "rejected" to 5))
        },
    )
