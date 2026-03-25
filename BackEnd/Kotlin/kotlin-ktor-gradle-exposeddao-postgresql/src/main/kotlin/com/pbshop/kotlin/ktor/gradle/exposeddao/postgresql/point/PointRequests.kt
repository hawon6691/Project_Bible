package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import kotlinx.serialization.Serializable

@Serializable
data class PointGrantRequest(
    val userId: Int,
    val amount: Int,
    val description: String,
    val expiresAt: String? = null,
)
