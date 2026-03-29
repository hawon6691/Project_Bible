package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import java.time.Instant

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
}

data class MediaAssetRecord(
    val id: Int,
    val uploaderId: Int,
    val ownerType: String,
    val ownerId: Int?,
    val originalName: String,
    val fileKey: String,
    val fileUrl: String,
    val type: MediaType,
    val mime: String,
    val size: Long,
    val duration: Int?,
    val width: Int?,
    val height: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class MediaUploadItem(
    val originalName: String,
    val mime: String,
    val size: Long,
)

data class MediaUploadCommand(
    val uploaderId: Int,
    val ownerType: String,
    val ownerId: Int?,
    val files: List<MediaUploadItem>,
)

data class CreatePresignedUrlCommand(
    val userId: Int,
    val fileName: String,
    val fileType: String,
    val fileSize: Long?,
)

data class PresignedUrlRecord(
    val uploadUrl: String,
    val fileKey: String,
    val expiresInSec: Int,
)

interface MediaRepository {
    fun upload(command: MediaUploadCommand): List<MediaAssetRecord>

    fun createPresignedUrl(command: CreatePresignedUrlCommand): PresignedUrlRecord

    fun findById(id: Int): MediaAssetRecord?

    fun delete(id: Int): Boolean
}
