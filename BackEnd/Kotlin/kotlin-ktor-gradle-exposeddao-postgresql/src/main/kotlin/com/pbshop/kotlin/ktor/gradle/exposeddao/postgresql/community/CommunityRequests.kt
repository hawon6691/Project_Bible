package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val title: String,
    val content: String,
)

@Serializable
data class PostUpdateRequest(
    val title: String? = null,
    val content: String? = null,
)

@Serializable
data class CommentRequest(
    val parentId: Int? = null,
    val content: String,
)
