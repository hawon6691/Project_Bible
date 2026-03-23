package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.commerce

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun commerceOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/cart", "Cart", "Get cart", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "productId" to 1, "quantity" to 2, "price" to 1590000)))
        },
        endpoint(HttpMethod.Post, "/cart", "Cart", "Add cart item", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "productId" to 2, "quantity" to 1))
        },
        endpoint(HttpMethod.Patch, "/cart/{itemId}", "Cart", "Update cart item", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("itemId", "1"), "quantity" to 3))
        },
        endpoint(HttpMethod.Delete, "/cart/{itemId}", "Cart", "Delete cart item", roles = setOf(PbRole.USER)) { message("Cart item removed.") },
        endpoint(HttpMethod.Delete, "/cart", "Cart", "Clear cart", roles = setOf(PbRole.USER)) { message("Cart cleared.") },
        endpoint(HttpMethod.Get, "/addresses", "Address", "Address list", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "recipient" to "PB User", "city" to "Seoul", "isDefault" to true)))
        },
        endpoint(HttpMethod.Post, "/addresses", "Address", "Create address", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "recipient" to "PB User", "city" to "Busan"))
        },
        endpoint(HttpMethod.Patch, "/addresses/{id}", "Address", "Update address", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "recipient" to "PB User", "city" to "Seoul"))
        },
        endpoint(HttpMethod.Delete, "/addresses/{id}", "Address", "Delete address", roles = setOf(PbRole.USER)) { message("Address deleted.") },
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
        endpoint(HttpMethod.Post, "/payments", "Payment", "Create payment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "PAYMENT_CONFIRMED", "provider" to "PBPAY"))
        },
        endpoint(HttpMethod.Get, "/payments/{id}", "Payment", "Payment detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "PAYMENT_CONFIRMED"))
        },
        endpoint(HttpMethod.Post, "/payments/{id}/refund", "Payment", "Refund payment", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "REFUND_REQUESTED"))
        },
        endpoint(HttpMethod.Get, "/products/{productId}/reviews", "Review", "Product reviews") { paged(listOf(mapOf("id" to 1, "rating" to 5, "content" to "Great product."))) },
        endpoint(HttpMethod.Post, "/products/{productId}/reviews", "Review", "Create product review", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 10, "rating" to 5, "awardedPoint" to 500))
        },
        endpoint(HttpMethod.Patch, "/reviews/{id}", "Review", "Update review", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "content" to "Updated review"))
        },
        endpoint(HttpMethod.Delete, "/reviews/{id}", "Review", "Delete review", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Review deleted.") },
        endpoint(HttpMethod.Get, "/wishlist", "Wishlist", "Wishlist list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("productId" to 1, "wishlistedAt" to "2026-03-23T10:00:00Z")))
        },
        endpoint(HttpMethod.Post, "/wishlist/{productId}", "Wishlist", "Toggle wishlist", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("wishlisted" to true)) },
        endpoint(HttpMethod.Delete, "/wishlist/{productId}", "Wishlist", "Delete wishlist item", roles = setOf(PbRole.USER)) { message("Wishlist item removed.") },
        endpoint(HttpMethod.Get, "/points/balance", "Point", "Point balance", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("balance" to 15000, "expiringSoon" to 3000, "expiringDate" to "2026-03-01"))
        },
        endpoint(HttpMethod.Get, "/points/transactions", "Point", "Point transactions", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "type" to "EARN", "amount" to 500, "balance" to 15000)))
        },
        endpoint(HttpMethod.Post, "/admin/points/grant", "Point", "Grant points", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "type" to "ADMIN_GRANT", "amount" to 1000))
        },
    )
