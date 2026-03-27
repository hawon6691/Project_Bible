package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import kotlinx.serialization.Serializable

@Serializable
data class SellerReviewCreateRequest(
    val orderId: Int,
    val rating: Int,
    val deliveryRating: Int,
    val content: String,
)

@Serializable
data class SellerReviewUpdateRequest(
    val rating: Int? = null,
    val deliveryRating: Int? = null,
    val content: String? = null,
)
