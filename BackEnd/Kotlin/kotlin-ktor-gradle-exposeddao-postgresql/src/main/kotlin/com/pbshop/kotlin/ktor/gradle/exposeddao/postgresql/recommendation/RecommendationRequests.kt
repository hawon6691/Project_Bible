package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationCreateRequest(
    val productId: Int,
    val type: String = "TODAY",
    val sortOrder: Int = 0,
    val startDate: String? = null,
    val endDate: String? = null,
)
