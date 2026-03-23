package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun activityOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/activity/views", "Activity", "Recently viewed products", roles = setOf(PbRole.USER)) { paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"))) },
        endpoint(HttpMethod.Delete, "/activity/views", "Activity", "Clear view history", roles = setOf(PbRole.USER)) { message("View history cleared.") },
        endpoint(HttpMethod.Get, "/activity/searches", "Activity", "Search history", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "keyword" to "galaxybook"), mapOf("id" to 2, "keyword" to "7800x3d")))
        },
        endpoint(HttpMethod.Delete, "/activity/searches", "Activity", "Clear search history", roles = setOf(PbRole.USER)) { message("Search history cleared.") },
        endpoint(HttpMethod.Delete, "/activity/searches/{id}", "Activity", "Delete search history entry", roles = setOf(PbRole.USER)) { message("Search history entry deleted.") },
    )
