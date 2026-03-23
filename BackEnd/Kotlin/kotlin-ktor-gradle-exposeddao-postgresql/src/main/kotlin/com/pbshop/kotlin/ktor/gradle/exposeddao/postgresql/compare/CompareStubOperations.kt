package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import io.ktor.http.HttpMethod

fun compareOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/compare/add", "Compare", "Add compare item") {
            StubResponse(data = mapOf("compareList" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Delete, "/compare/{productId}", "Compare", "Delete compare item") {
            StubResponse(data = mapOf("compareList" to emptyList<String>()))
        },
        endpoint(HttpMethod.Get, "/compare", "Compare", "Compare list") {
            StubResponse(data = mapOf("compareList" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Get, "/compare/detail", "Compare", "Compare detail") {
            StubResponse(data = mapOf("items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), "highlights" to listOf("weight", "battery")))
        },
    )
