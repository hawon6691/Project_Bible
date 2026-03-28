package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import java.time.Instant

class InMemoryImageRepository(
    assets: List<ImageAssetRecord>,
) : ImageRepository {
    private val assets = linkedMapOf<Int, ImageAssetRecord>()
    private var assetSequence = 1
    private var variantSequence = 1

    init {
        assets.forEach { asset ->
            this.assets[asset.id] = asset
            assetSequence = maxOf(assetSequence, asset.id + 1)
            asset.variants.forEach { variantSequence = maxOf(variantSequence, it.id + 1) }
        }
    }

    override fun uploadImage(command: ImageUploadCommand): ImageAssetRecord {
        val now = Instant.now()
        val token = "${now.toEpochMilli()}-${assetSequence}"
        val extension = extensionFor(command.originalFilename)
        val storedFilename = "$token.$extension"
        val originalUrl = "/uploads/original/$storedFilename"
        val assetId = assetSequence++
        val variants =
            listOf(
                variant(assetId, "THUMBNAIL", token, "webp", 200, 200, (command.size * 0.12).toInt().coerceAtLeast(1)),
                variant(assetId, "MEDIUM", token, "webp", 600, 600, (command.size * 0.35).toInt().coerceAtLeast(1)),
                variant(assetId, "LARGE", token, "webp", 1200, 1200, (command.size * 0.65).toInt().coerceAtLeast(1)),
            )
        val saved =
            ImageAssetRecord(
                id = assetId,
                uploadedByUserId = command.uploadedByUserId,
                originalFilename = command.originalFilename,
                storedFilename = storedFilename,
                originalUrl = originalUrl,
                mimeType = command.mimeType,
                size = command.size,
                category = command.category,
                processingStatus = "COMPLETED",
                createdAt = now,
                updatedAt = now,
                variants = variants,
            )
        assets[saved.id] = saved
        return saved
    }

    override fun findImageById(id: Int): ImageAssetRecord? = assets[id]

    override fun listVariants(imageId: Int): List<ImageVariantRecord> = assets[imageId]?.variants.orEmpty()

    override fun deleteImage(id: Int): Boolean = assets.remove(id) != null

    private fun variant(
        imageId: Int,
        type: String,
        token: String,
        format: String,
        width: Int,
        height: Int,
        size: Int,
    ): ImageVariantRecord {
        val id = variantSequence++
        val path = when (type) {
            "THUMBNAIL" -> "thumb"
            "MEDIUM" -> "medium"
            else -> "large"
        }
        val now = Instant.now()
        return ImageVariantRecord(
            id = id,
            imageId = imageId,
            type = type,
            url = "/uploads/$path/$token.$format",
            format = format,
            width = width,
            height = height,
            size = size,
            createdAt = now,
        )
    }

    private fun extensionFor(filename: String): String =
        filename.substringAfterLast('.', "bin").lowercase()

    companion object {
        fun seeded(): InMemoryImageRepository {
            val now = Instant.parse("2026-02-11T09:00:00Z")
            return InMemoryImageRepository(
                assets =
                    listOf(
                        ImageAssetRecord(
                            id = 1,
                            uploadedByUserId = 4,
                            originalFilename = "sample-product.jpg",
                            storedFilename = "sample-product.jpg",
                            originalUrl = "/uploads/original/sample-product.jpg",
                            mimeType = "image/jpeg",
                            size = 78200,
                            category = "product",
                            processingStatus = "COMPLETED",
                            createdAt = now,
                            updatedAt = now,
                            variants =
                                listOf(
                                    ImageVariantRecord(1, 1, "THUMBNAIL", "/uploads/thumb/sample-product.webp", "webp", 200, 200, 8540, now),
                                    ImageVariantRecord(2, 1, "MEDIUM", "/uploads/medium/sample-product.webp", "webp", 600, 600, 32100, now),
                                    ImageVariantRecord(3, 1, "LARGE", "/uploads/large/sample-product.webp", "webp", 1200, 1200, 78200, now),
                                ),
                        ),
                    ),
            )
        }
    }
}
