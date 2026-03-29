package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.MediaAssetsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoMediaRepository(
    private val databaseFactory: DatabaseFactory,
) : MediaRepository {
    override fun upload(command: MediaUploadCommand): List<MediaAssetRecord> =
        databaseFactory.withTransaction {
            val now = Instant.now()
            command.files.map { file ->
                val key = "${command.uploaderId}/${now.toEpochMilli()}-${file.originalName.replace("\\s+".toRegex(), "-")}"
                val insertedId =
                    MediaAssetsTable.insert {
                        it[MediaAssetsTable.uploaderId] = command.uploaderId
                        it[MediaAssetsTable.ownerType] = command.ownerType
                        it[MediaAssetsTable.ownerId] = command.ownerId
                        it[MediaAssetsTable.originalName] = file.originalName
                        it[MediaAssetsTable.fileKey] = key
                        it[MediaAssetsTable.fileUrl] = "/uploads/media/$key"
                        it[MediaAssetsTable.type] = resolveType(file.mime).name
                        it[MediaAssetsTable.mime] = file.mime
                        it[MediaAssetsTable.size] = file.size
                        it[MediaAssetsTable.duration] = if (file.mime.startsWith("video/") || file.mime.startsWith("audio/")) 0 else null
                        it[MediaAssetsTable.width] = if (file.mime.startsWith("image/") || file.mime.startsWith("video/")) 1920 else null
                        it[MediaAssetsTable.height] = if (file.mime.startsWith("image/") || file.mime.startsWith("video/")) 1080 else null
                        it[MediaAssetsTable.deletedAt] = null
                        it[MediaAssetsTable.createdAt] = now
                        it[MediaAssetsTable.updatedAt] = now
                    } get MediaAssetsTable.id
                MediaAssetsTable.selectAll().where { MediaAssetsTable.id eq insertedId.value }.single().let(::toRecord)
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

    override fun findById(id: Int): MediaAssetRecord? =
        databaseFactory.withTransaction {
            MediaAssetsTable.selectAll()
                .where { (MediaAssetsTable.id eq id) and MediaAssetsTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let(::toRecord)
        }

    override fun delete(id: Int): Boolean =
        databaseFactory.withTransaction {
            val updated =
                MediaAssetsTable.update({ (MediaAssetsTable.id eq id) and MediaAssetsTable.deletedAt.isNull() }) {
                    it[MediaAssetsTable.deletedAt] = Instant.now()
                    it[MediaAssetsTable.updatedAt] = Instant.now()
                }
            updated > 0
        }

    private fun resolveType(mime: String): MediaType =
        when {
            mime.startsWith("image/") -> MediaType.IMAGE
            mime.startsWith("video/") -> MediaType.VIDEO
            mime.startsWith("audio/") -> MediaType.AUDIO
            else -> MediaType.DOCUMENT
        }

    private fun toRecord(row: ResultRow): MediaAssetRecord =
        MediaAssetRecord(
            id = row[MediaAssetsTable.id].value,
            uploaderId = row[MediaAssetsTable.uploaderId],
            ownerType = row[MediaAssetsTable.ownerType],
            ownerId = row[MediaAssetsTable.ownerId],
            originalName = row[MediaAssetsTable.originalName],
            fileKey = row[MediaAssetsTable.fileKey],
            fileUrl = row[MediaAssetsTable.fileUrl],
            type = MediaType.valueOf(row[MediaAssetsTable.type]),
            mime = row[MediaAssetsTable.mime],
            size = row[MediaAssetsTable.size],
            duration = row[MediaAssetsTable.duration],
            width = row[MediaAssetsTable.width],
            height = row[MediaAssetsTable.height],
            createdAt = row[MediaAssetsTable.createdAt],
            updatedAt = row[MediaAssetsTable.updatedAt],
        )
}
