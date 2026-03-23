package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun paymentOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/payments", "Payment", "Create payment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "PAYMENT_CONFIRMED", "provider" to "PBPAY"))
        },
        endpoint(HttpMethod.Get, "/payments/{id}", "Payment", "Payment detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "PAYMENT_CONFIRMED"))
        },
        endpoint(HttpMethod.Post, "/payments/{id}/refund", "Payment", "Refund payment", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "REFUND_REQUESTED"))
        },
    )
