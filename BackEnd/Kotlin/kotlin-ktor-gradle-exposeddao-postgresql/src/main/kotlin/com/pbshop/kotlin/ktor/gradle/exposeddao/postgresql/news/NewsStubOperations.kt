package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun newsOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/news", "News", "News list") {
            paged(listOf(mapOf("id" to 1, "title" to "PB weekly hardware briefing")))
        },
        endpoint(HttpMethod.Get, "/news/categories", "News", "News category list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Hardware")))
        },
        endpoint(HttpMethod.Get, "/news/{id}", "News", "News detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "PB weekly hardware briefing", "relatedProducts" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Post, "/news", "News", "Create news", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New article"))
        },
        endpoint(HttpMethod.Patch, "/news/{id}", "News", "Update news", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated article"))
        },
        endpoint(HttpMethod.Delete, "/news/{id}", "News", "Delete news", roles = setOf(PbRole.ADMIN)) {
            message("News deleted.")
        },
        endpoint(HttpMethod.Post, "/news/categories", "News", "Create news category", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Deals"))
        },
        endpoint(HttpMethod.Delete, "/news/categories/{id}", "News", "Delete news category", roles = setOf(PbRole.ADMIN)) {
            message("News category deleted.")
        },
    )
