package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun cartOperations(): List<StubOperation> =
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
    )
