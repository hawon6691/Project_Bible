package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

enum class ImageProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
}

enum class ImageVariantType {
    THUMBNAIL,
    MEDIUM,
    LARGE,
}

object TranslationsTable : IntIdTable("translations") {
    val locale = varchar("locale", 10)
    val namespace = varchar("namespace", 100)
    val translationKey = varchar("translation_key", 191)
    val translationValue = text("translation_value")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ExchangeRatesTable : IntIdTable("exchange_rates") {
    val baseCurrency = varchar("base_currency", 10)
    val targetCurrency = varchar("target_currency", 10)
    val rate = decimal("rate", 18, 8)
    val updatedAtExchange = timestamp("updated_at_exchange").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ImageAssetsTable : IntIdTable("image_assets") {
    val uploadedByUserId = reference("uploaded_by_user_id", UsersTable).nullable()
    val originalFilename = varchar("original_filename", 255)
    val storedFilename = varchar("stored_filename", 255)
    val originalUrl = varchar("original_url", 500)
    val mimeType = varchar("mime_type", 100)
    val size = integer("size")
    val category = varchar("category", 20)
    val processingStatus = varchar("processing_status", 40)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class ImageAssetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageAssetEntity>(ImageAssetsTable)

    var uploadedByUserId by ImageAssetsTable.uploadedByUserId
    var originalFilename by ImageAssetsTable.originalFilename
    var storedFilename by ImageAssetsTable.storedFilename
    var originalUrl by ImageAssetsTable.originalUrl
    var mimeType by ImageAssetsTable.mimeType
    var size by ImageAssetsTable.size
    var category by ImageAssetsTable.category
    var processingStatus by ImageAssetsTable.processingStatus
    var createdAt by ImageAssetsTable.createdAt
    var updatedAt by ImageAssetsTable.updatedAt
}

object ImageVariantsTable : IntIdTable("image_variants") {
    val image = reference("image_id", ImageAssetsTable)
    val type = varchar("type", 40)
    val url = varchar("url", 500)
    val format = varchar("format", 20)
    val width = integer("width")
    val height = integer("height")
    val size = integer("size")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class ImageVariantEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageVariantEntity>(ImageVariantsTable)

    var imageId by ImageVariantsTable.image
    var type by ImageVariantsTable.type
    var url by ImageVariantsTable.url
    var format by ImageVariantsTable.format
    var width by ImageVariantsTable.width
    var height by ImageVariantsTable.height
    var size by ImageVariantsTable.size
    var createdAt by ImageVariantsTable.createdAt
    var updatedAt by ImageVariantsTable.updatedAt
}
