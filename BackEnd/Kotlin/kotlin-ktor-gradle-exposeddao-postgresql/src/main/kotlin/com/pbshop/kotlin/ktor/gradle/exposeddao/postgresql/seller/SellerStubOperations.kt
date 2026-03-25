package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun sellerOperations() =
    listOf(
        endpoint(HttpMethod.Get, "/sellers", "Seller", "Seller list") { message("Seller list contract") },
        endpoint(HttpMethod.Get, "/sellers/{id}", "Seller", "Seller detail") { message("Seller detail contract") },
        endpoint(HttpMethod.Post, "/sellers", "Seller", "Create seller", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "created"))
        },
        endpoint(HttpMethod.Patch, "/sellers/{id}", "Seller", "Update seller", roles = setOf(PbRole.ADMIN)) { message("Seller updated") },
        endpoint(HttpMethod.Delete, "/sellers/{id}", "Seller", "Delete seller", roles = setOf(PbRole.ADMIN)) { message("Seller deleted") },
    )
