package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ImageAssetEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ImageAssetsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ImageVariantEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ImageVariantsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoImageRepository(
    private val databaseFactory: DatabaseFactory,
) : ImageRepository {
    override fun uploadImage(command: ImageUploadCommand): ImageAssetRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val token = "${now.toEpochMilli()}-${command.originalFilename.hashCode().toString().replace('-', '0')}"
            val extension = command.originalFilename.substringAfterLast('.', "bin").lowercase()
            val storedFilename = "$token.$extension"
            val created =
                ImageAssetEntity.new {
                    uploadedByUserId = command.uploadedByUserId?.let { EntityID(it, UsersTable) }
                    originalFilename = command.originalFilename
                    this.storedFilename = storedFilename
                    originalUrl = "/uploads/original/$storedFilename"
                    mimeType = command.mimeType
                    size = command.size
                    category = command.category
                    processingStatus = "PROCESSING"
                    createdAt = now
                    updatedAt = now
                }
            createVariants(created.id.value, token, command.size, now)
            created.processingStatus = "COMPLETED"
            created.updatedAt = now
            requireNotNull(findImageById(created.id.value))
        }

    override fun findImageById(id: Int): ImageAssetRecord? =
        databaseFactory.withTransaction {
            ImageAssetEntity.findById(id)?.let { asset ->
                ImageAssetRecord(
                    id = asset.id.value,
                    uploadedByUserId = asset.uploadedByUserId?.value,
                    originalFilename = asset.originalFilename,
                    storedFilename = asset.storedFilename,
                    originalUrl = asset.originalUrl,
                    mimeType = asset.mimeType,
                    size = asset.size,
                    category = asset.category,
                    processingStatus = asset.processingStatus,
                    createdAt = asset.createdAt,
                    updatedAt = asset.updatedAt,
                    variants = listVariants(asset.id.value),
                )
            }
        }

    override fun listVariants(imageId: Int): List<ImageVariantRecord> =
        databaseFactory.withTransaction {
            ImageVariantsTable.selectAll()
                .where { ImageVariantsTable.image eq imageId }
                .orderBy(ImageVariantsTable.id to SortOrder.ASC)
                .map {
                    ImageVariantRecord(
                        id = it[ImageVariantsTable.id].value,
                        imageId = it[ImageVariantsTable.image].value,
                        type = it[ImageVariantsTable.type],
                        url = it[ImageVariantsTable.url],
                        format = it[ImageVariantsTable.format],
                        width = it[ImageVariantsTable.width],
                        height = it[ImageVariantsTable.height],
                        size = it[ImageVariantsTable.size],
                        createdAt = it[ImageVariantsTable.createdAt],
                    )
                }
        }

    override fun deleteImage(id: Int): Boolean =
        databaseFactory.withTransaction {
            val asset = ImageAssetEntity.findById(id) ?: return@withTransaction false
            ImageVariantEntity.find { ImageVariantsTable.image eq id }.forEach { it.delete() }
            asset.delete()
            true
        }

    private fun createVariants(
        imageId: Int,
        token: String,
        originalSize: Int,
        now: Instant,
    ) {
        listOf(
            VariantSeed("THUMBNAIL", "thumb", 200, 200, 0.12),
            VariantSeed("MEDIUM", "medium", 600, 600, 0.35),
            VariantSeed("LARGE", "large", 1200, 1200, 0.65),
        ).forEach { variant ->
            ImageVariantEntity.new {
                this.imageId = EntityID(imageId, ImageAssetsTable)
                type = variant.type
                url = "/uploads/${variant.path}/$token.webp"
                format = "webp"
                width = variant.width
                height = variant.height
                size = (originalSize * variant.sizeRatio).toInt().coerceAtLeast(1)
                createdAt = now
                updatedAt = now
            }
        }
    }

    private data class VariantSeed(
        val type: String,
        val path: String,
        val width: Int,
        val height: Int,
        val sizeRatio: Double,
    )
}
