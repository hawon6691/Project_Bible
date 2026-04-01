package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import kotlinx.serialization.Serializable

@Serializable
data class AllowedExtensionsRecord(
    val extensions: List<String>,
)

@Serializable
data class UploadLimitsRecord(
    val image: Int,
    val video: Int,
    val audio: Int,
)

@Serializable
data class ReviewPolicyRecord(
    val maxImageCount: Int,
    val pointAmount: Int,
)

interface AdminSettingsRepository {
    fun getAllowedExtensions(): AllowedExtensionsRecord

    fun setAllowedExtensions(extensions: List<String>): AllowedExtensionsRecord

    fun getUploadLimits(): UploadLimitsRecord

    fun updateUploadLimits(
        image: Int? = null,
        video: Int? = null,
        audio: Int? = null,
    ): UploadLimitsRecord

    fun getReviewPolicy(): ReviewPolicyRecord

    fun updateReviewPolicy(
        maxImageCount: Int,
        pointAmount: Int,
    ): ReviewPolicyRecord
}
