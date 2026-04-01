package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import kotlinx.serialization.Serializable

@Serializable
data class AutoEstimateRequest(
    val modelId: Int,
    val trimId: Int,
    val optionIds: List<Int> = emptyList(),
)
