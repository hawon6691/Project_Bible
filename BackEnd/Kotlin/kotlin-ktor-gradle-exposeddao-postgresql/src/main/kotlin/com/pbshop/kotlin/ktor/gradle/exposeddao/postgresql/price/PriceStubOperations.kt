package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun priceOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/products/{id}/prices", "Price", "Product prices") {
            StubResponse(data = listOf(mapOf("sellerId" to 1, "sellerName" to "PB Mall", "price" to 1590000), mapOf("sellerId" to 2, "sellerName" to "Fast Delivery", "price" to 1585000)))
        },
        endpoint(HttpMethod.Get, "/products/{id}/price-history", "Price", "Product price history") {
            StubResponse(data = listOf(mapOf("date" to "2026-03-20", "price" to 1600000), mapOf("date" to "2026-03-21", "price" to 1595000), mapOf("date" to "2026-03-22", "price" to 1590000)))
        },
        endpoint(HttpMethod.Post, "/price-alerts", "Price", "Create price alert", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "targetPrice" to 1490000, "enabled" to true))
        },
        endpoint(HttpMethod.Get, "/price-alerts", "Price", "Price alert list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "productId" to 1, "targetPrice" to 1490000)))
        },
    )
