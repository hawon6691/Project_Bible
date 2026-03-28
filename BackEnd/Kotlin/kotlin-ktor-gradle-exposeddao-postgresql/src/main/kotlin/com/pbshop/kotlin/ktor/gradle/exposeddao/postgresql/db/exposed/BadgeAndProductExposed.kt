package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

enum class ProductStatus {
    ON_SALE,
    SOLD_OUT,
    HIDDEN,
}

enum class ShippingType {
    FREE,
    PAID,
    CONDITIONAL,
}

object ProductsTable : IntIdTable("products") {
    val name = varchar("name", 200)
    val description = text("description")
    val price = integer("price")
    val discountPrice = integer("discount_price").nullable()
    val stock = integer("stock")
    val status = pgEnum<ProductStatus>("status", "product_status")
    val category = reference("category_id", CategoriesTable)
    val thumbnailUrl = varchar("thumbnail_url", 500).nullable()
    val lowestPrice = integer("lowest_price").nullable()
    val sellerCount = integer("seller_count")
    val viewCount = integer("view_count")
    val reviewCount = integer("review_count")
    val averageRating = decimal("average_rating", 2, 1)
    val salesCount = integer("sales_count")
    val popularityScore = decimal("popularity_score", 10, 2)
    val version = integer("version")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class ProductEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductEntity>(ProductsTable)

    var name by ProductsTable.name
    var description by ProductsTable.description
    var price by ProductsTable.price
    var discountPrice by ProductsTable.discountPrice
    var stock by ProductsTable.stock
    var status by ProductsTable.status
    var categoryId by ProductsTable.category
    var thumbnailUrl by ProductsTable.thumbnailUrl
    var lowestPrice by ProductsTable.lowestPrice
    var sellerCount by ProductsTable.sellerCount
    var viewCount by ProductsTable.viewCount
    var reviewCount by ProductsTable.reviewCount
    var averageRating by ProductsTable.averageRating
    var salesCount by ProductsTable.salesCount
    var popularityScore by ProductsTable.popularityScore
    var version by ProductsTable.version
    var createdAt by ProductsTable.createdAt
    var updatedAt by ProductsTable.updatedAt
    var deletedAt by ProductsTable.deletedAt
}

object ProductOptionsTable : IntIdTable("product_options") {
    val product = reference("product_id", ProductsTable)
    val name = varchar("name", 50)
    val optionValues = text("values")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class ProductOptionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductOptionEntity>(ProductOptionsTable)

    var productId by ProductOptionsTable.product
    var name by ProductOptionsTable.name
    var optionValuesJson by ProductOptionsTable.optionValues
    var createdAt by ProductOptionsTable.createdAt
    var updatedAt by ProductOptionsTable.updatedAt
}

object ProductImagesTable : IntIdTable("product_images") {
    val product = reference("product_id", ProductsTable)
    val url = varchar("url", 500)
    val isMain = bool("is_main")
    val sortOrder = integer("sort_order")
    val imageVariantId = integer("image_variant_id").nullable()
    val createdAt = timestamp("created_at")
}

class ProductImageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductImageEntity>(ProductImagesTable)

    var productId by ProductImagesTable.product
    var url by ProductImagesTable.url
    var isMain by ProductImagesTable.isMain
    var sortOrder by ProductImagesTable.sortOrder
    var imageVariantId by ProductImagesTable.imageVariantId
    var createdAt by ProductImagesTable.createdAt
}

object SpecDefinitionsTable : IntIdTable("spec_definitions") {
    val category = reference("category_id", CategoriesTable)
    val name = varchar("name", 50)
    val type = varchar("type", 20)
    val options = text("options").nullable()
    val unit = varchar("unit", 20).nullable()
    val isComparable = bool("is_comparable")
    val dataType = varchar("data_type", 20)
    val sortOrder = integer("sort_order")
    val createdAt = timestamp("created_at")
}

object ProductSpecsTable : IntIdTable("product_specs") {
    val product = reference("product_id", ProductsTable)
    val specDefinition = reference("spec_definition_id", SpecDefinitionsTable)
    val value = varchar("value", 200)
    val numericValue = decimal("numeric_value", 10, 2).nullable()
    val createdAt = timestamp("created_at")
}

object SpecScoresTable : IntIdTable("spec_scores") {
    val specDefinition = reference("spec_definition_id", SpecDefinitionsTable)
    val value = varchar("value", 200)
    val score = integer("score")
    val benchmarkSource = varchar("benchmark_source", 100).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object SellersTable : IntIdTable("sellers") {
    val name = varchar("name", 100)
    val url = varchar("url", 500)
    val logoUrl = varchar("logo_url", 500).nullable()
    val trustScore = integer("trust_score")
    val trustGrade = varchar("trust_grade", 2).nullable()
    val description = varchar("description", 200).nullable()
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object PriceEntriesTable : IntIdTable("price_entries") {
    val product = reference("product_id", ProductsTable)
    val seller = reference("seller_id", SellersTable)
    val price = integer("price")
    val shippingCost = integer("shipping_cost")
    val shippingInfo = varchar("shipping_info", 100).nullable()
    val productUrl = varchar("product_url", 1000)
    val shippingFee = integer("shipping_fee")
    val shippingType = pgEnum<ShippingType>("shipping_type", "shipping_type")
    val totalPrice = integer("total_price")
    val clickCount = integer("click_count")
    val isAvailable = bool("is_available")
    val crawledAt = timestamp("crawled_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object PriceHistoryTable : IntIdTable("price_history") {
    val product = reference("product_id", ProductsTable)
    val date = date("date")
    val lowestPrice = integer("lowest_price")
    val averagePrice = integer("average_price")
    val highestPrice = integer("highest_price")
    val createdAt = timestamp("created_at")
}

object PriceAlertsTable : IntIdTable("price_alerts") {
    val user = reference("user_id", UsersTable)
    val product = reference("product_id", ProductsTable)
    val targetPrice = integer("target_price")
    val isTriggered = bool("is_triggered")
    val triggeredAt = timestamp("triggered_at").nullable()
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
}

object BadgesTable : IntIdTable("badges") {
    val name = varchar("name", 50)
    val description = varchar("description", 200)
    val iconUrl = varchar("icon_url", 500)
    val type = varchar("type", 20)
    val conditionJson = text("condition").nullable()
    val rarity = varchar("rarity", 20)
    val holderCount = integer("holder_count")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object UserBadgesTable : IntIdTable("user_badges") {
    val user = reference("user_id", UsersTable)
    val badge = reference("badge_id", BadgesTable)
    val grantedByAdminId = reference("granted_by_admin_id", UsersTable).nullable()
    val reason = varchar("reason", 255).nullable()
    val grantedAt = timestamp("granted_at")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
