package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import kotlinx.serialization.Serializable

@Serializable
data class PriceEntryRequest(
    val sellerId: Int,
    val price: Int,
    val shippingCost: Int = 0,
    val shippingInfo: String? = null,
    val productUrl: String,
    val shippingFee: Int = 0,
    val shippingType: String = "PAID",
    val isAvailable: Boolean = true,
)

@Serializable
data class PriceEntryUpdateRequest(
    val sellerId: Int? = null,
    val price: Int? = null,
    val shippingCost: Int? = null,
    val shippingInfo: String? = null,
    val productUrl: String? = null,
    val shippingFee: Int? = null,
    val shippingType: String? = null,
    val isAvailable: Boolean? = null,
)

@Serializable
data class PriceAlertRequest(
    val productId: Int,
    val targetPrice: Int,
    val isActive: Boolean = true,
)
