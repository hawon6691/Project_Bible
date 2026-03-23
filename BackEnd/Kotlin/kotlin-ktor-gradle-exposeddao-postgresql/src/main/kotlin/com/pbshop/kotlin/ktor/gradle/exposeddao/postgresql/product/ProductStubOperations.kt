package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun productOperations() =
    listOf(
        endpoint(HttpMethod.Get, "/products", "Product", "Product list") { message("Product list contract") },
        endpoint(HttpMethod.Get, "/products/{id}", "Product", "Product detail") { message("Product detail contract") },
        endpoint(HttpMethod.Post, "/products", "Product", "Create product", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "created"))
        },
        endpoint(HttpMethod.Patch, "/products/{id}", "Product", "Update product", roles = setOf(PbRole.ADMIN)) { message("Product updated") },
        endpoint(HttpMethod.Delete, "/products/{id}", "Product", "Delete product", roles = setOf(PbRole.ADMIN)) { message("Product deleted") },
        endpoint(HttpMethod.Post, "/products/{id}/options", "Product", "Create product option", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "option created"))
        },
        endpoint(HttpMethod.Patch, "/products/{id}/options/{optionId}", "Product", "Update product option", roles = setOf(PbRole.ADMIN)) { message("Product option updated") },
        endpoint(HttpMethod.Delete, "/products/{id}/options/{optionId}", "Product", "Delete product option", roles = setOf(PbRole.ADMIN)) { message("Product option deleted") },
        endpoint(HttpMethod.Post, "/products/{id}/images", "Product", "Upload product image", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "image created"))
        },
        endpoint(HttpMethod.Delete, "/products/{id}/images/{imageId}", "Product", "Delete product image", roles = setOf(PbRole.ADMIN)) { message("Product image deleted") },
    )
