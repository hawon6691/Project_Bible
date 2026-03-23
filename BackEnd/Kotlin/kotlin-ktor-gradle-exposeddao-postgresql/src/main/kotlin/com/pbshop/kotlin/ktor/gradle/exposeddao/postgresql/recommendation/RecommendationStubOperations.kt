package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun recommendationOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/recommendations/today", "Recommendation", "Today recommendations") {
            StubResponse(data = listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")))
        },
        endpoint(HttpMethod.Get, "/recommendations/personalized", "Recommendation", "Personalized recommendations", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(productSummary(3, "PB Gaming Laptop 17")))
        },
        endpoint(HttpMethod.Get, "/admin/recommendations", "Recommendation", "Admin recommendation list", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = listOf(mapOf("id" to 1, "productId" to 1, "slot" to "today")))
        },
        endpoint(HttpMethod.Post, "/admin/recommendations", "Recommendation", "Create recommendation", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "productId" to 2, "slot" to "today"))
        },
        endpoint(HttpMethod.Delete, "/admin/recommendations/{id}", "Recommendation", "Delete recommendation", roles = setOf(PbRole.ADMIN)) {
            message("Recommendation deleted.")
        },
    )
