package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun priceOperations() =
    listOf(
        endpoint(HttpMethod.Get, "/products/{id}/prices", "Price", "Product prices") { message("Product prices contract") },
        endpoint(HttpMethod.Post, "/products/{id}/prices", "Price", "Create product price", roles = setOf(PbRole.SELLER, PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "created"))
        },
        endpoint(HttpMethod.Patch, "/prices/{id}", "Price", "Update price", roles = setOf(PbRole.SELLER, PbRole.ADMIN)) { message("Price updated") },
        endpoint(HttpMethod.Delete, "/prices/{id}", "Price", "Delete price", roles = setOf(PbRole.ADMIN)) { message("Price deleted") },
        endpoint(HttpMethod.Get, "/products/{id}/price-history", "Price", "Product price history") { message("Price history contract") },
        endpoint(HttpMethod.Post, "/price-alerts", "Price", "Create price alert", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "created"))
        },
        endpoint(HttpMethod.Get, "/price-alerts", "Price", "Price alert list", roles = setOf(PbRole.USER)) { message("Price alert list contract") },
    )
