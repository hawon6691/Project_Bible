package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun queryOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/query/products", "Query", "Query product read model") {
            paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro")))
        },
        endpoint(HttpMethod.Get, "/query/products/{productId}", "Query", "Query product read model detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("productId", "1"), "name" to "PB GalaxyBook 4 Pro", "source" to "query-model"))
        },
        endpoint(HttpMethod.Post, "/admin/query/products/{productId}/sync", "Query", "Sync single query product", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("productId" to call.pathParam("productId", "1"), "queued" to true))
        },
        endpoint(HttpMethod.Post, "/admin/query/products/rebuild", "Query", "Rebuild query read model", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("queued" to true, "scope" to "all-products"))
        },
    )
