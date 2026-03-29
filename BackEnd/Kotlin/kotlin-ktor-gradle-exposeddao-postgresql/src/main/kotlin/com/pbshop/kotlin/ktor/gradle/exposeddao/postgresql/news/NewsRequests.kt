package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import kotlinx.serialization.Serializable

@Serializable
data class CreateNewsRequest(
    val title: String,
    val content: String,
    val categoryId: Int,
    val thumbnailUrl: String? = null,
    val productIds: List<Int> = emptyList(),
)

@Serializable
data class UpdateNewsRequest(
    val title: String? = null,
    val content: String? = null,
    val categoryId: Int? = null,
    val thumbnailUrl: String? = null,
    val productIds: List<Int>? = null,
)

@Serializable
data class CreateNewsCategoryRequest(
    val name: String,
    val slug: String,
)
