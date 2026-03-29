package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import kotlinx.serialization.Serializable

@Serializable
data class CreatePresignedUrlRequest(
    val fileName: String,
    val fileType: String,
    val fileSize: Long? = null,
)
