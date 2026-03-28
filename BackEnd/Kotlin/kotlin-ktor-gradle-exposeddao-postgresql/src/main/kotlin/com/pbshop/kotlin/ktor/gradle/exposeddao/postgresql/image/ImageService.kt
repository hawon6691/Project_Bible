package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

data class ImageUploadInput(
    val uploadedByUserId: Int?,
    val originalFilename: String,
    val mimeType: String,
    val size: Int,
    val category: String,
)

class ImageService(
    private val repository: ImageRepository,
) {
    fun upload(input: ImageUploadInput): StubResponse {
        val saved = storeImage(input)
        return StubResponse(status = HttpStatusCode.Created, data = assetPayload(saved))
    }

    fun uploadLegacy(input: ImageUploadInput): StubResponse {
        val saved = storeImage(input)
        return StubResponse(status = HttpStatusCode.Created, data = mapOf("url" to saved.originalUrl))
    }

    fun variants(imageId: Int): StubResponse {
        requireImage(imageId)
        return StubResponse(data = repository.listVariants(imageId).map(::variantPayload))
    }

    fun delete(imageId: Int): StubResponse {
        if (!repository.deleteImage(imageId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "이미지를 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("message" to "이미지가 삭제되었습니다."))
    }

    fun storeImage(input: ImageUploadInput): ImageAssetRecord {
        validateInput(input)
        return repository.uploadImage(
            ImageUploadCommand(
                uploadedByUserId = input.uploadedByUserId,
                originalFilename = sanitizeFilename(input.originalFilename),
                mimeType = input.mimeType.lowercase(),
                size = input.size,
                category = normalizeCategory(input.category),
            ),
        )
    }

    fun preferredUrl(asset: ImageAssetRecord): String =
        asset.variants.firstOrNull { it.type == "LARGE" }?.url
            ?: asset.variants.firstOrNull { it.type == "MEDIUM" }?.url
            ?: asset.variants.firstOrNull()?.url
            ?: asset.originalUrl

    fun preferredVariantId(asset: ImageAssetRecord): Int? =
        asset.variants.firstOrNull { it.type == "LARGE" }?.id
            ?: asset.variants.firstOrNull { it.type == "MEDIUM" }?.id
            ?: asset.variants.firstOrNull()?.id

    private fun requireImage(imageId: Int): ImageAssetRecord =
        repository.findImageById(imageId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "이미지를 찾을 수 없습니다.")

    private fun validateInput(input: ImageUploadInput) {
        if (input.originalFilename.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "FILE_UPLOAD_FAILED", "업로드할 파일이 필요합니다.")
        }
        if (input.size <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "FILE_UPLOAD_FAILED", "업로드할 파일이 필요합니다.")
        }
        if (input.size > MAX_FILE_SIZE) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "최대 10MB 파일만 업로드할 수 있습니다.")
        }
        if (!ALLOWED_MIME_TYPES.contains(input.mimeType.lowercase())) {
            throw PbShopException(HttpStatusCode.BadRequest, "FILE_TYPE_NOT_ALLOWED", "허용되지 않는 이미지 형식입니다.")
        }
        normalizeCategory(input.category)
    }

    private fun sanitizeFilename(filename: String): String =
        filename.trim().ifBlank { "image.bin" }.replace("\\s+".toRegex(), "-")

    private fun normalizeCategory(category: String): String {
        val normalized = category.trim().lowercase()
        if (!ALLOWED_CATEGORIES.contains(normalized)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "지원하지 않는 이미지 카테고리입니다.")
        }
        return normalized
    }

    private fun assetPayload(asset: ImageAssetRecord): Map<String, Any?> =
        mapOf(
            "id" to asset.id,
            "originalUrl" to asset.originalUrl,
            "variants" to asset.variants.map(::variantPayload),
            "processingStatus" to asset.processingStatus,
        )

    private fun variantPayload(variant: ImageVariantRecord): Map<String, Any?> =
        mapOf(
            "id" to variant.id,
            "imageId" to variant.imageId,
            "type" to variant.type,
            "url" to variant.url,
            "width" to variant.width,
            "height" to variant.height,
            "format" to variant.format,
            "size" to variant.size,
            "createdAt" to variant.createdAt.toString(),
        )

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024
        private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
        private val ALLOWED_CATEGORIES = setOf("product", "community", "support", "seller", "profile")
    }
}
