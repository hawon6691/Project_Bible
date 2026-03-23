package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun fraudOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/fraud/alerts", "Fraud", "Fraud alerts", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "status" to "OPEN", "productId" to 1)))
        },
        endpoint(HttpMethod.Patch, "/fraud/alerts/{id}/approve", "Fraud", "Approve fraud alert", roles = setOf(PbRole.ADMIN)) {
            message("Fraud alert approved.")
        },
        endpoint(HttpMethod.Patch, "/fraud/alerts/{id}/reject", "Fraud", "Reject fraud alert", roles = setOf(PbRole.ADMIN)) {
            message("Fraud alert rejected.")
        },
        endpoint(HttpMethod.Get, "/products/{id}/real-price", "Fraud", "Real price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "productPrice" to 1500000, "shippingFee" to 3000, "totalPrice" to 1503000))
        },
    )
