package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun wishlistOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/wishlist", "Wishlist", "Wishlist list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("productId" to 1, "wishlistedAt" to "2026-03-23T10:00:00Z")))
        },
        endpoint(HttpMethod.Post, "/wishlist/{productId}", "Wishlist", "Toggle wishlist", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("wishlisted" to true)) },
        endpoint(HttpMethod.Delete, "/wishlist/{productId}", "Wishlist", "Delete wishlist item", roles = setOf(PbRole.USER)) { message("Wishlist item removed.") },
    )
