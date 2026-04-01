package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import kotlinx.serialization.Serializable

@Serializable
data class SetAllowedExtensionsRequest(
    val extensions: List<String>,
)

@Serializable
data class UpdateUploadLimitsRequest(
    val image: Int? = null,
    val video: Int? = null,
    val audio: Int? = null,
)

@Serializable
data class UpdateReviewPolicyRequest(
    val maxImageCount: Int,
    val pointAmount: Int,
)
