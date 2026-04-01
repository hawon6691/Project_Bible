package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object SystemSettingsTable : IntIdTable("system_settings") {
    val settingGroup = varchar("setting_group", 100)
    val settingKey = varchar("setting_key", 100)
    val settingValue = text("setting_value")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex("uk_system_settings_group_key", settingGroup, settingKey)
    }
}

object ProductQueryViewsTable : IntIdTable("product_query_views") {
    val product = reference("product_id", ProductsTable).uniqueIndex("idx_product_query_views_product_id")
    val categoryId = integer("category_id").index("idx_product_query_views_category_id")
    val name = varchar("name", 200)
    val thumbnailUrl = varchar("thumbnail_url", 500).nullable()
    val status = varchar("status", 20)
    val basePrice = integer("base_price")
    val lowestPrice = integer("lowest_price").nullable()
    val sellerCount = integer("seller_count")
    val averageRating = decimal("average_rating", 3, 2)
    val reviewCount = integer("review_count")
    val viewCount = integer("view_count")
    val popularityScore = decimal("popularity_score", 10, 2)
    val syncedAt = timestamp("synced_at")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
