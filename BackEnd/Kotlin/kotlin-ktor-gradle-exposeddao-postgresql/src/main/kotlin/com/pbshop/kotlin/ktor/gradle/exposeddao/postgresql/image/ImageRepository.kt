package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import java.time.Instant

data class ImageVariantRecord(
    val id: Int,
    val imageId: Int,
    val type: String,
    val url: String,
    val format: String,
    val width: Int,
    val height: Int,
    val size: Int,
    val createdAt: Instant,
)

data class ImageAssetRecord(
    val id: Int,
    val uploadedByUserId: Int?,
    val originalFilename: String,
    val storedFilename: String,
    val originalUrl: String,
    val mimeType: String,
    val size: Int,
    val category: String,
    val processingStatus: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val variants: List<ImageVariantRecord>,
)

data class ImageUploadCommand(
    val uploadedByUserId: Int?,
    val originalFilename: String,
    val mimeType: String,
    val size: Int,
    val category: String,
)

interface ImageRepository {
    fun uploadImage(command: ImageUploadCommand): ImageAssetRecord

    fun findImageById(id: Int): ImageAssetRecord?

    fun listVariants(imageId: Int): List<ImageVariantRecord>

    fun deleteImage(id: Int): Boolean
}
