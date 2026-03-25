package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import kotlinx.serialization.Serializable

@Serializable
data class SellerRequest(
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val trustScore: Int = 0,
    val trustGrade: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
)

@Serializable
data class SellerUpdateRequest(
    val name: String? = null,
    val url: String? = null,
    val logoUrl: String? = null,
    val trustScore: Int? = null,
    val trustGrade: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null,
)
