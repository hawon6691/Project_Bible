package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val productId: Int,
    val sellerId: Int,
    val quantity: Int,
    val selectedOptions: String? = null,
)

@Serializable
data class OrderCreateRequest(
    val addressId: Int,
    val items: List<OrderItemRequest> = emptyList(),
    val fromCart: Boolean = false,
    val cartItemIds: List<Int> = emptyList(),
    val usePoint: Int = 0,
    val memo: String? = null,
)

@Serializable
data class OrderStatusUpdateRequest(
    val status: String,
)
