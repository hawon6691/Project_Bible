package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import kotlinx.serialization.Serializable

@Serializable
data class CategoryCreateRequest(
    val name: String,
    val parentId: Int? = null,
    val sortOrder: Int? = null,
)

@Serializable
data class CategoryUpdateRequest(
    val name: String? = null,
    val sortOrder: Int? = null,
)
