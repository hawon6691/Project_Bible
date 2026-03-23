package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun productOperations(): List<StubOperation> =
    listOf(
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
    )
