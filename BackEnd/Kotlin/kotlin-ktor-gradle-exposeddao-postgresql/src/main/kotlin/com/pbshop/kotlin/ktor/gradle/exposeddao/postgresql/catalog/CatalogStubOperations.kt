package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.catalog

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun catalogOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/categories", "Category", "Category list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Laptop", "slug" to "laptop"), mapOf("id" to 2, "name" to "PC Parts", "slug" to "pc-parts")))
        },
        endpoint(HttpMethod.Post, "/categories", "Category", "Create category", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "name" to "Tablet", "slug" to "tablet"))
        },
        endpoint(HttpMethod.Get, "/products", "Product", "Product list") {
            paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), totalCount = 2)
        },
        endpoint(HttpMethod.Get, "/products/{id}", "Product", "Product detail") { call ->
            StubResponse(
                data =
                    mapOf(
                        "id" to call.pathParam("id", "1"),
                        "name" to "PB GalaxyBook 4 Pro",
                        "description" to "Kotlin baseline product detail payload.",
                        "lowestPrice" to 1590000,
                        "thumbnailUrl" to "/images/products/1-thumb.webp",
                        "specs" to mapOf("cpu" to "Ryzen 7", "ram" to "32GB"),
                    ),
            )
        },
        endpoint(HttpMethod.Post, "/products", "Product", "Create product", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = productSummary(101, "PB New Product"))
        },
        endpoint(HttpMethod.Get, "/specs/definitions", "Spec", "Specification definitions") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "CPU", "categoryId" to 2), mapOf("id" to 2, "name" to "RAM", "categoryId" to 2)))
        },
        endpoint(HttpMethod.Post, "/specs/compare", "Spec", "Compare specifications") {
            StubResponse(data = mapOf("items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), "diff" to listOf("cpu", "weight", "battery")))
        },
        endpoint(HttpMethod.Post, "/specs/compare/scored", "Spec", "Compare specifications with score") {
            StubResponse(data = mapOf("winnerProductId" to 1, "scores" to listOf(mapOf("productId" to 1, "score" to 91), mapOf("productId" to 2, "score" to 88))))
        },
        endpoint(HttpMethod.Get, "/sellers", "Seller", "Seller list") {
            paged(listOf(mapOf("id" to 1, "name" to "PB Mall", "trustScore" to 95), mapOf("id" to 2, "name" to "Fast Delivery", "trustScore" to 92)))
        },
        endpoint(HttpMethod.Post, "/sellers", "Seller", "Create seller", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "name" to "New Seller"))
        },
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
