package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

class InMemoryAdminSettingsRepository private constructor(
    private var allowedExtensions: AllowedExtensionsRecord,
    private var uploadLimits: UploadLimitsRecord,
    private var reviewPolicy: ReviewPolicyRecord,
) : AdminSettingsRepository {
    override fun getAllowedExtensions(): AllowedExtensionsRecord = allowedExtensions

    override fun setAllowedExtensions(extensions: List<String>): AllowedExtensionsRecord {
        allowedExtensions = AllowedExtensionsRecord(extensions)
        return allowedExtensions
    }

    override fun getUploadLimits(): UploadLimitsRecord = uploadLimits

    override fun updateUploadLimits(image: Int?, video: Int?, audio: Int?): UploadLimitsRecord {
        uploadLimits =
            uploadLimits.copy(
                image = image ?: uploadLimits.image,
                video = video ?: uploadLimits.video,
                audio = audio ?: uploadLimits.audio,
            )
        return uploadLimits
    }

    override fun getReviewPolicy(): ReviewPolicyRecord = reviewPolicy

    override fun updateReviewPolicy(maxImageCount: Int, pointAmount: Int): ReviewPolicyRecord {
        reviewPolicy = ReviewPolicyRecord(maxImageCount = maxImageCount, pointAmount = pointAmount)
        return reviewPolicy
    }

    companion object {
        fun seeded(): InMemoryAdminSettingsRepository =
            InMemoryAdminSettingsRepository(
                allowedExtensions = AllowedExtensionsRecord(listOf("gif", "jpeg", "jpg", "mp3", "mp4", "pdf", "png", "webp")),
                uploadLimits = UploadLimitsRecord(image = 5, video = 100, audio = 20),
                reviewPolicy = ReviewPolicyRecord(maxImageCount = 10, pointAmount = 500),
            )
    }
}
