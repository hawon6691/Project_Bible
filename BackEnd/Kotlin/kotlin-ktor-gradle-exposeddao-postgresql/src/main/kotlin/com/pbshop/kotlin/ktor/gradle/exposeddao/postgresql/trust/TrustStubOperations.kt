package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun trustOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/sellers/{id}/trust", "Trust", "Seller trust detail") { call ->
            StubResponse(data = mapOf("sellerId" to call.pathParam("id", "1"), "overallScore" to 95, "grade" to "A+"))
        },
        endpoint(HttpMethod.Get, "/sellers/{id}/reviews", "Trust", "Seller reviews") {
            paged(listOf(mapOf("id" to 1, "rating" to 5, "content" to "Fast delivery")))
        },
        endpoint(HttpMethod.Post, "/sellers/{id}/reviews", "Trust", "Create seller review", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "rating" to 5))
        },
        endpoint(HttpMethod.Patch, "/seller-reviews/{id}", "Trust", "Update seller review", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "rating" to 4))
        },
        endpoint(HttpMethod.Delete, "/seller-reviews/{id}", "Trust", "Delete seller review", roles = setOf(PbRole.USER, PbRole.ADMIN)) {
            message("Seller review deleted.")
        },
    )
