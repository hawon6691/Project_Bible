package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun dealOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/deals", "Deal", "Deal list") {
            paged(listOf(mapOf("id" to 1, "title" to "Spring laptop sale", "discountRate" to 15)))
        },
        endpoint(HttpMethod.Get, "/deals/{id}", "Deal", "Deal detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Spring laptop sale", "products" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Post, "/deals", "Deal", "Create deal", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New deal"))
        },
        endpoint(HttpMethod.Patch, "/deals/{id}", "Deal", "Update deal", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated deal"))
        },
        endpoint(HttpMethod.Delete, "/deals/{id}", "Deal", "Delete deal", roles = setOf(PbRole.ADMIN)) {
            message("Deal deleted.")
        },
    )
