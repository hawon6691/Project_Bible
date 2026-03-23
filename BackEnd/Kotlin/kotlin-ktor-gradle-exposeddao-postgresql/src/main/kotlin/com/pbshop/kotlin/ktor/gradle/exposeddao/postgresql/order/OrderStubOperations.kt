package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun orderOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/orders", "Order", "Create order", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "orderNumber" to "ORD-20260323-KOTLIN", "status" to "ORDER_PLACED", "finalAmount" to 3175000))
        },
        endpoint(HttpMethod.Get, "/orders", "Order", "Order list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "orderNumber" to "ORD-20260323-KOTLIN", "status" to "PAYMENT_CONFIRMED")))
        },
        endpoint(HttpMethod.Get, "/orders/{id}", "Order", "Order detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "PAYMENT_CONFIRMED", "items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Post, "/orders/{id}/cancel", "Order", "Cancel order", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "CANCELLED"))
        },
        endpoint(HttpMethod.Get, "/admin/orders", "Order", "Admin order list", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "status" to "PAYMENT_CONFIRMED"), mapOf("id" to 2, "status" to "SHIPPING")))
        },
        endpoint(HttpMethod.Patch, "/admin/orders/{id}/status", "Order", "Admin update order status", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "SHIPPING"))
        },
    )
