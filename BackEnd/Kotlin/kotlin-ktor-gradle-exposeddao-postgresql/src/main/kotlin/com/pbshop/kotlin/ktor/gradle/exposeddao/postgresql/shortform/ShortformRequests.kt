package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import kotlinx.serialization.Serializable

@Serializable
data class CreateShortformCommentRequest(
    val content: String,
)

data class ShortformUploadInput(
    val title: String,
    val originalFilename: String,
    val mimeType: String,
    val size: Int,
    val productIds: List<Int>,
)
