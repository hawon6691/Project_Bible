package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import kotlinx.serialization.Serializable

@Serializable
data class SpecDefinitionRequest(
    val categoryId: Int,
    val name: String,
    val type: String,
    val options: List<String>? = null,
    val unit: String? = null,
    val isComparable: Boolean = true,
    val dataType: String = "STRING",
    val sortOrder: Int = 0,
)

@Serializable
data class SpecDefinitionUpdateRequest(
    val categoryId: Int? = null,
    val name: String? = null,
    val type: String? = null,
    val options: List<String>? = null,
    val unit: String? = null,
    val isComparable: Boolean? = null,
    val dataType: String? = null,
    val sortOrder: Int? = null,
)

@Serializable
data class ProductSpecValueRequest(
    val specDefinitionId: Int,
    val value: String,
    val numericValue: Double? = null,
)

@Serializable
data class SpecCompareRequest(
    val productIds: List<Int>,
)

@Serializable
data class SpecScoreRequest(
    val value: String,
    val score: Int,
    val benchmarkSource: String? = null,
)
