package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun sellerOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/sellers", "Seller", "Seller list") {
            paged(listOf(mapOf("id" to 1, "name" to "PB Mall", "trustScore" to 95), mapOf("id" to 2, "name" to "Fast Delivery", "trustScore" to 92)))
        },
        endpoint(HttpMethod.Post, "/sellers", "Seller", "Create seller", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "name" to "New Seller"))
        },
    )
