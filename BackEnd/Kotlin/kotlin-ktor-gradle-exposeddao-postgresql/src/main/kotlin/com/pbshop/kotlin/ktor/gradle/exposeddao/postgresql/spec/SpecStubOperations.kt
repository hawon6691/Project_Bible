package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import io.ktor.http.HttpMethod

fun specOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/specs/definitions", "Spec", "Specification definitions") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "CPU", "categoryId" to 2), mapOf("id" to 2, "name" to "RAM", "categoryId" to 2)))
        },
        endpoint(HttpMethod.Post, "/specs/compare", "Spec", "Compare specifications") {
            StubResponse(data = mapOf("items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), "diff" to listOf("cpu", "weight", "battery")))
        },
        endpoint(HttpMethod.Post, "/specs/compare/scored", "Spec", "Compare specifications with score") {
            StubResponse(data = mapOf("winnerProductId" to 1, "scores" to listOf(mapOf("productId" to 1, "score" to 91), mapOf("productId" to 2, "score" to 88))))
        },
    )
