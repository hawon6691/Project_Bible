package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun reviewOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/products/{productId}/reviews", "Review", "Product reviews") { paged(listOf(mapOf("id" to 1, "rating" to 5, "content" to "Great product."))) },
        endpoint(HttpMethod.Post, "/products/{productId}/reviews", "Review", "Create product review", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 10, "rating" to 5, "awardedPoint" to 500))
        },
        endpoint(HttpMethod.Patch, "/reviews/{id}", "Review", "Update review", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "content" to "Updated review"))
        },
        endpoint(HttpMethod.Delete, "/reviews/{id}", "Review", "Delete review", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Review deleted.") },
    )
