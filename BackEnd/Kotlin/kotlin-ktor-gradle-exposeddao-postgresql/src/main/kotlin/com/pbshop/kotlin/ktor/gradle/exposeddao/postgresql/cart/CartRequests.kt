package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import kotlinx.serialization.Serializable

@Serializable
data class CartCreateRequest(
    val productId: Int,
    val sellerId: Int,
    val quantity: Int,
    val selectedOptions: String? = null,
)

@Serializable
data class CartUpdateRequest(
    val quantity: Int,
)
