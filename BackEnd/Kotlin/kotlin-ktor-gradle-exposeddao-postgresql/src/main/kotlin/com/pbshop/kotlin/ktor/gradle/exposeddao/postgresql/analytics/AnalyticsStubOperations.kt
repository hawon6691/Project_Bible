package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import io.ktor.http.HttpMethod

fun analyticsOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/analytics/products/{id}/lowest-ever", "Analytics", "Lowest ever price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "isLowestEver" to true, "lowestPrice" to 1490000))
        },
        endpoint(HttpMethod.Get, "/analytics/products/{id}/unit-price", "Analytics", "Unit price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "unitPrice" to 12450, "unit" to "GB"))
        },
    )
