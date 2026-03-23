package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import io.ktor.http.HttpMethod

fun predictionOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/predictions/products/{productId}/price-trend", "Prediction", "Price trend prediction") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("productId", "1"), "trend" to "FALLING", "recommendation" to "BUY_SOON", "predictions" to listOf(mapOf("date" to "2026-03-24", "predictedPrice" to 1585000, "confidence" to 0.85))))
        },
    )
