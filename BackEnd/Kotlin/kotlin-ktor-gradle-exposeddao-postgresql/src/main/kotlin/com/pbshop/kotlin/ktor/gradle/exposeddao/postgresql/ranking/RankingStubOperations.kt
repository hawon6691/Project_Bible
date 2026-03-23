package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import io.ktor.http.HttpMethod

fun rankingOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/rankings/products/popular", "Ranking", "Popular product ranking") {
            StubResponse(data = listOf(mapOf("rank" to 1, "product" to productSummary(1, "PB GalaxyBook 4 Pro"), "score" to 15230)))
        },
        endpoint(HttpMethod.Get, "/rankings/searches", "Ranking", "Popular search ranking") {
            StubResponse(data = listOf(mapOf("rank" to 1, "keyword" to "galaxybook", "searchCount" to 5230)))
        },
    )
