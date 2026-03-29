package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import kotlinx.serialization.Serializable

@Serializable
data class ApproveMappingRequest(
    val productId: Int,
)

@Serializable
data class RejectMappingRequest(
    val reason: String,
)
