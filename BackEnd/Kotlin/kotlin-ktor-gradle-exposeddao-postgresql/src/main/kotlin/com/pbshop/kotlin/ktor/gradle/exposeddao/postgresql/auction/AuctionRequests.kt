package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import kotlinx.serialization.Serializable
@Serializable
data class CreateAuctionRequest(
    val title: String,
    val description: String,
    val categoryId: Int? = null,
    val specs: String? = null,
    val budget: Int? = null,
    val expiresAt: String? = null,
)

@Serializable
data class CreateAuctionBidRequest(
    val price: Int,
    val description: String? = null,
    val deliveryDays: Int,
)

@Serializable
data class UpdateAuctionBidRequest(
    val price: Int? = null,
    val description: String? = null,
    val deliveryDays: Int? = null,
)
