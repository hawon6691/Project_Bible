package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun searchOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/search", "Search", "Search catalog") {
            StubResponse(
                data = mapOf("hits" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")), "facets" to mapOf("categories" to listOf(mapOf("id" to 1, "name" to "Laptop", "count" to 12))), "suggestions" to listOf("galaxybook", "galaxybook 4"), "totalCount" to 1),
                meta = mapOf("page" to 1, "limit" to 20, "totalCount" to 1, "totalPages" to 1),
            )
        },
        endpoint(HttpMethod.Get, "/search/autocomplete", "Search", "Search autocomplete") {
            StubResponse(data = mapOf("keywords" to listOf("galaxybook 4 pro"), "products" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")), "categories" to listOf(mapOf("id" to 1, "name" to "Laptop"))))
        },
        endpoint(HttpMethod.Get, "/search/popular", "Search", "Popular search keywords") {
            StubResponse(data = listOf(mapOf("keyword" to "galaxybook", "count" to 5230)))
        },
        endpoint(HttpMethod.Post, "/search/recent", "Search", "Save recent search", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "keyword" to "galaxybook", "createdAt" to "2026-03-23T10:00:00Z"))
        },
        endpoint(HttpMethod.Get, "/search/recent", "Search", "Recent searches", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "keyword" to "galaxybook"), mapOf("id" to 2, "keyword" to "7800x3d")))
        },
        endpoint(HttpMethod.Delete, "/search/recent/{id}", "Search", "Delete recent search", roles = setOf(PbRole.USER)) {
            message("Recent search deleted.")
        },
        endpoint(HttpMethod.Delete, "/search/recent", "Search", "Delete all recent searches", roles = setOf(PbRole.USER)) {
            message("Recent search history cleared.")
        },
        endpoint(HttpMethod.Patch, "/search/preferences", "Search", "Update search preferences", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("saveRecentSearches" to true))
        },
        endpoint(HttpMethod.Get, "/search/admin/weights", "Search", "Search weights", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("nameWeight" to 1.0, "keywordWeight" to 1.5, "clickWeight" to 0.8))
        },
        endpoint(HttpMethod.Patch, "/search/admin/weights", "Search", "Update search weights", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("nameWeight" to 1.2, "keywordWeight" to 1.4, "clickWeight" to 0.9))
        },
        endpoint(HttpMethod.Get, "/search/admin/index/status", "Search", "Search index status", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("status" to "HEALTHY", "documents" to 1200, "lastIndexedAt" to "2026-03-23T09:55:00Z"))
        },
        endpoint(HttpMethod.Post, "/search/admin/index/reindex", "Search", "Reindex search", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "Reindex queued.", "queued" to true))
        },
        endpoint(HttpMethod.Post, "/search/admin/index/products/{id}/reindex", "Search", "Reindex product", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "Product reindex queued.", "productId" to call.pathParam("id", "1")))
        },
    )
