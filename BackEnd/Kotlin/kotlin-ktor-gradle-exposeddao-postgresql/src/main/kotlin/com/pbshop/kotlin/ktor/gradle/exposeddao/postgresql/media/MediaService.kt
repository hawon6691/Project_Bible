package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class MediaService(
    private val repository: MediaRepository,
) {
    fun upload(
        userId: Int,
        ownerType: String,
        ownerId: Int?,
        files: List<MediaUploadItem>,
    ): StubResponse {
        if (files.isEmpty()) {
            throw PbShopException(HttpStatusCode.BadRequest, "FILE_UPLOAD_FAILED", "업로드할 파일이 필요합니다.")
        }
        if (ownerType.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "ownerType이 필요합니다.")
        }
        files.forEach(::validateFile)
        val saved =
            repository.upload(
                MediaUploadCommand(
                    uploaderId = userId,
                    ownerType = ownerType.trim(),
                    ownerId = ownerId,
                    files = files,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = saved.map(::attachmentPayload))
    }

    fun createPresignedUrl(userId: Int, request: CreatePresignedUrlRequest): StubResponse {
        if (request.fileName.trim().isBlank() || request.fileType.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "fileName과 fileType이 필요합니다.")
        }
        val created =
            repository.createPresignedUrl(
                CreatePresignedUrlCommand(
                    userId = userId,
                    fileName = request.fileName.trim(),
                    fileType = request.fileType.trim(),
                    fileSize = request.fileSize,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = mapOf("uploadUrl" to created.uploadUrl, "fileKey" to created.fileKey, "expiresInSec" to created.expiresInSec))
    }

    fun metadata(id: Int): StubResponse {
        val media = requireMedia(id)
        return StubResponse(data = metadataPayload(media))
    }

    fun stream(id: Int): StubResponse {
        val media = requireMedia(id)
        return StubResponse(
            status = HttpStatusCode.PartialContent,
            data =
                mapOf(
                    "id" to media.id,
                    "fileUrl" to media.fileUrl,
                    "mime" to media.mime,
                    "size" to media.size,
                    "duration" to media.duration,
                    "resolution" to resolution(media),
                ),
            meta = mapOf("streamable" to true),
        )
    }

    fun delete(userId: Int, isAdmin: Boolean, id: Int): StubResponse {
        val media = requireMedia(id)
        if (!isAdmin && media.uploaderId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "삭제 권한이 없습니다.")
        }
        repository.delete(id)
        return StubResponse(data = mapOf("success" to true, "message" to "파일이 삭제되었습니다."))
    }

    private fun requireMedia(id: Int): MediaAssetRecord =
        repository.findById(id)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "미디어 자산을 찾을 수 없습니다.")

    private fun validateFile(file: MediaUploadItem) {
        if (file.originalName.trim().isBlank() || file.size <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "FILE_UPLOAD_FAILED", "업로드할 파일이 필요합니다.")
        }
    }

    private fun attachmentPayload(record: MediaAssetRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "ownerType" to record.ownerType,
            "ownerId" to record.ownerId,
            "fileKey" to record.fileKey,
            "fileUrl" to record.fileUrl,
            "type" to record.type.name,
            "mime" to record.mime,
            "size" to record.size,
            "duration" to record.duration,
            "resolution" to resolution(record),
            "createdAt" to record.createdAt.toString(),
        )

    private fun metadataPayload(record: MediaAssetRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "mime" to record.mime,
            "size" to record.size,
            "duration" to record.duration,
            "resolution" to resolution(record),
            "ownerType" to record.ownerType,
            "ownerId" to record.ownerId,
            "uploadedAt" to record.createdAt.toString(),
        )

    private fun resolution(record: MediaAssetRecord): String? =
        if (record.width != null && record.height != null) "${record.width}x${record.height}" else null
}
