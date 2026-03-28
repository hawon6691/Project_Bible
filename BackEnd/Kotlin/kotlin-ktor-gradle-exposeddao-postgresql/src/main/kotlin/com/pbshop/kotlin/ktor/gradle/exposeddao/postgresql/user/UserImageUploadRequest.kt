package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

data class UserImageUploadRequest(
    val fileName: String,
    val mimeType: String,
    val size: Int,
)
