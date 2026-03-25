package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import kotlinx.serialization.Serializable

@Serializable
data class ReviewRequest(
    val orderId: Int,
    val rating: Int,
    val content: String,
)

@Serializable
data class ReviewUpdateRequest(
    val rating: Int? = null,
    val content: String? = null,
)
