package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import java.time.Instant

class InMemoryMediaRepository(
    mediaAssets: List<MediaAssetRecord>,
) : MediaRepository {
    private val assets = linkedMapOf<Int, MediaAssetRecord>()
    private val deleted = mutableSetOf<Int>()
    private var sequence = 1

    init {
        mediaAssets.forEach {
            assets[it.id] = it
            sequence = maxOf(sequence, it.id + 1)
        }
    }

    override fun upload(command: MediaUploadCommand): List<MediaAssetRecord> {
        val now = Instant.now()
        return command.files.map { file ->
            val id = sequence++
            val key = "${command.uploaderId}/${now.toEpochMilli()}-$id-${file.originalName.replace("\\s+".toRegex(), "-")}"
            MediaAssetRecord(
                id = id,
                uploaderId = command.uploaderId,
                ownerType = command.ownerType,
                ownerId = command.ownerId,
                originalName = file.originalName,
                fileKey = key,
                fileUrl = "/uploads/media/$key",
                type = resolveType(file.mime),
                mime = file.mime,
                size = file.size,
                duration = if (file.mime.startsWith("video/") || file.mime.startsWith("audio/")) 0 else null,
                width = if (file.mime.startsWith("image/") || file.mime.startsWith("video/")) 1920 else null,
                height = if (file.mime.startsWith("image/") || file.mime.startsWith("video/")) 1080 else null,
                createdAt = now,
                updatedAt = now,
            ).also { assets[it.id] = it }
        }
    }

    override fun createPresignedUrl(command: CreatePresignedUrlCommand): PresignedUrlRecord {
        val safeName = command.fileName.replace("\\s+".toRegex(), "-")
        val fileKey = "presigned/${command.userId}/${Instant.now().toEpochMilli()}-$safeName"
        return PresignedUrlRecord(
            uploadUrl = "https://example-storage.local/upload/$fileKey",
            fileKey = fileKey,
            expiresInSec = 900,
        )
    }

    override fun findById(id: Int): MediaAssetRecord? = assets[id]?.takeIf { !deleted.contains(id) }

    override fun delete(id: Int): Boolean {
        if (!assets.containsKey(id) || deleted.contains(id)) {
            return false
        }
        deleted += id
        return true
    }

    private fun resolveType(mime: String): MediaType =
        when {
            mime.startsWith("image/") -> MediaType.IMAGE
            mime.startsWith("video/") -> MediaType.VIDEO
            mime.startsWith("audio/") -> MediaType.AUDIO
            else -> MediaType.DOCUMENT
        }

    companion object {
        fun seeded(): InMemoryMediaRepository {
            val now = Instant.parse("2026-03-20T10:00:00Z")
            return InMemoryMediaRepository(
                listOf(
                    MediaAssetRecord(
                        id = 1,
                        uploaderId = 4,
                        ownerType = "PRODUCT",
                        ownerId = 1,
                        originalName = "gaming-laptop.jpg",
                        fileKey = "media/4/gaming-laptop.jpg",
                        fileUrl = "/uploads/media/media/4/gaming-laptop.jpg",
                        type = MediaType.IMAGE,
                        mime = "image/jpeg",
                        size = 2048,
                        duration = null,
                        width = 1920,
                        height = 1080,
                        createdAt = now,
                        updatedAt = now,
                    ),
                    MediaAssetRecord(
                        id = 2,
                        uploaderId = 5,
                        ownerType = "NEWS",
                        ownerId = 1,
                        originalName = "launch.mp4",
                        fileKey = "media/5/launch.mp4",
                        fileUrl = "/uploads/media/media/5/launch.mp4",
                        type = MediaType.VIDEO,
                        mime = "video/mp4",
                        size = 8_388_608,
                        duration = 45,
                        width = 1920,
                        height = 1080,
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
            )
        }
    }
}
