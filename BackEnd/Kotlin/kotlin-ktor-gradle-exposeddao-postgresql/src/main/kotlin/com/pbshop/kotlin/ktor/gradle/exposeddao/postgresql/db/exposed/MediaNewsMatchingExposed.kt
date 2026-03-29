package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object MediaAssetsTable : IntIdTable("media_assets") {
    val uploaderId = integer("uploader_id")
    val ownerType = varchar("owner_type", 50)
    val ownerId = integer("owner_id").nullable()
    val originalName = varchar("original_name", 255)
    val fileKey = varchar("file_key", 255)
    val fileUrl = varchar("file_url", 500)
    val type = varchar("type", 20)
    val mime = varchar("mime", 100)
    val size = long("size")
    val duration = integer("duration").nullable()
    val width = integer("width").nullable()
    val height = integer("height").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object NewsCategoriesTable : IntIdTable("news_categories") {
    val name = varchar("name", 100)
    val slug = varchar("slug", 120)
    val deletedAt = timestamp("deleted_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object NewsTable : IntIdTable("news") {
    val title = varchar("title", 255)
    val content = text("content")
    val categoryId = integer("category_id")
    val thumbnailUrl = varchar("thumbnail_url", 500).nullable()
    val viewCount = integer("view_count")
    val deletedAt = timestamp("deleted_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object NewsProductsTable : IntIdTable("news_products") {
    val newsId = integer("news_id")
    val productId = integer("product_id")
    val deletedAt = timestamp("deleted_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ProductMappingsTable : IntIdTable("product_mappings") {
    val sourceName = varchar("source_name", 255)
    val sourceBrand = varchar("source_brand", 120).nullable()
    val sourceSeller = varchar("source_seller", 120).nullable()
    val sourceUrl = varchar("source_url", 500).nullable()
    val productId = integer("product_id").nullable()
    val status = varchar("status", 20)
    val confidence = double("confidence")
    val reason = varchar("reason", 255).nullable()
    val reviewedBy = integer("reviewed_by").nullable()
    val reviewedAt = timestamp("reviewed_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
