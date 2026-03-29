package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import kotlinx.serialization.Serializable

@Serializable
data class CreatePcBuildRequest(
    val name: String,
    val description: String? = null,
    val purpose: String,
    val budget: Int? = null,
)

@Serializable
data class UpdatePcBuildRequest(
    val name: String? = null,
    val description: String? = null,
    val purpose: String? = null,
    val budget: Int? = null,
)

@Serializable
data class AddPcBuildPartRequest(
    val productId: Int,
    val partType: String,
    val sellerId: Int? = null,
    val quantity: Int = 1,
)

@Serializable
data class CreateCompatibilityRuleRequest(
    val partType: String,
    val targetPartType: String? = null,
    val title: String,
    val description: String,
    val severity: String = "MEDIUM",
    val enabled: Boolean = true,
    val metadata: Map<String, String>? = null,
)

@Serializable
data class UpdateCompatibilityRuleRequest(
    val partType: String? = null,
    val targetPartType: String? = null,
    val title: String? = null,
    val description: String? = null,
    val severity: String? = null,
    val enabled: Boolean? = null,
    val metadata: Map<String, String>? = null,
)
