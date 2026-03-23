package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import io.ktor.http.HttpMethod

fun errorCodeOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/errors/codes", "Error Code", "Error code catalog") {
            StubResponse(data = mapOf("total" to 4, "items" to listOf(mapOf("code" to "PRODUCT_NOT_FOUND", "message" to "Product not found"))))
        },
        endpoint(HttpMethod.Get, "/errors/codes/{key}", "Error Code", "Error code detail") { call ->
            StubResponse(data = mapOf("code" to call.pathParam("key", "PRODUCT_NOT_FOUND"), "message" to "Product not found"))
        },
    )
