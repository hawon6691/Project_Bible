package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import io.ktor.http.HttpMethod

fun autoOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/auto/models", "Auto", "Auto models") {
            StubResponse(data = listOf(mapOf("id" to 1, "brand" to "PB Motors", "name" to "Electro X")))
        },
        endpoint(HttpMethod.Get, "/auto/models/{id}/trims", "Auto", "Auto trims") { call ->
            StubResponse(data = listOf(mapOf("modelId" to call.pathParam("id", "1"), "name" to "Long Range")))
        },
        endpoint(HttpMethod.Post, "/auto/estimate", "Auto", "Auto estimate") {
            StubResponse(data = mapOf("basePrice" to 48000000, "optionPrice" to 3500000, "totalPrice" to 53200000))
        },
        endpoint(HttpMethod.Get, "/auto/models/{id}/lease-offers", "Auto", "Auto lease offers") { call ->
            StubResponse(data = listOf(mapOf("modelId" to call.pathParam("id", "1"), "provider" to "PB Lease", "monthlyPayment" to 450000)))
        },
    )
