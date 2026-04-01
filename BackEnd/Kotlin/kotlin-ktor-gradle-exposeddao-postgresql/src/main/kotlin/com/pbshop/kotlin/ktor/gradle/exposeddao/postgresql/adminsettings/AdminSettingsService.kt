package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class AdminSettingsService(
    private val repository: AdminSettingsRepository,
) {
    fun extensions(): StubResponse = StubResponse(data = repository.getAllowedExtensions())

    fun setExtensions(request: SetAllowedExtensionsRequest): StubResponse {
        val normalized =
            request.extensions
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        if (normalized.isEmpty()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "extensions가 필요합니다.")
        }
        return StubResponse(data = repository.setAllowedExtensions(normalized))
    }

    fun uploadLimits(): StubResponse = StubResponse(data = repository.getUploadLimits())

    fun updateUploadLimits(request: UpdateUploadLimitsRequest): StubResponse {
        if (listOf(request.image, request.video, request.audio).all { it == null }) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 용량 제한 값이 필요합니다.")
        }
        listOf(request.image, request.video, request.audio).filterNotNull().forEach {
            if (it <= 0) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "용량 제한은 1MB 이상이어야 합니다.")
            }
        }
        return StubResponse(
            data =
                repository.updateUploadLimits(
                    image = request.image,
                    video = request.video,
                    audio = request.audio,
                ),
        )
    }

    fun reviewPolicy(): StubResponse = StubResponse(data = repository.getReviewPolicy())

    fun updateReviewPolicy(request: UpdateReviewPolicyRequest): StubResponse {
        if (request.maxImageCount <= 0 || request.pointAmount < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효한 리뷰 정책 값이 필요합니다.")
        }
        return StubResponse(
            data =
                repository.updateReviewPolicy(
                    maxImageCount = request.maxImageCount,
                    pointAmount = request.pointAmount,
                ),
        )
    }
}
