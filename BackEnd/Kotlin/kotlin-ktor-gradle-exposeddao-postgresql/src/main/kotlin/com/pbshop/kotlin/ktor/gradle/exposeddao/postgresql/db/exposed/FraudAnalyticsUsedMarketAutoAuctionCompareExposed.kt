package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object FraudAlertsTable : IntIdTable("fraud_alerts") {
    val product = reference("product_id", ProductsTable)
    val seller = reference("seller_id", SellersTable).nullable()
    val priceEntry = reference("price_entry_id", PriceEntriesTable).nullable()
    val reason = varchar("reason", 255)
    val detectedPrice = integer("detected_price")
    val averagePrice = integer("average_price")
    val deviationPercent = decimal("deviation_percent", 5, 2)
    val status = varchar("status", 20)
    val reviewedBy = reference("reviewed_by", UsersTable).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object UsedPricesTable : IntIdTable("used_prices") {
    val product = reference("product_id", ProductsTable)
    val averagePrice = integer("average_price")
    val minPrice = integer("min_price")
    val maxPrice = integer("max_price")
    val sampleCount = integer("sample_count")
    val sourceName = varchar("source", 50)
    val collectedAt = timestamp("collected_at")
    val createdAt = timestamp("created_at")
}

object CarModelsTable : IntIdTable("car_models") {
    val brand = varchar("brand", 50)
    val name = varchar("name", 100)
    val type = varchar("type", 30)
    val year = integer("year")
    val basePrice = integer("base_price")
    val imageUrl = varchar("image_url", 500).nullable()
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object AutoTrimsTable : IntIdTable("auto_trims") {
    val carModel = reference("car_model_id", CarModelsTable)
    val name = varchar("name", 120)
    val basePrice = integer("base_price")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object AutoOptionsTable : IntIdTable("auto_options") {
    val trim = reference("trim_id", AutoTrimsTable)
    val name = varchar("name", 120)
    val price = integer("price")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object LeaseOffersTable : IntIdTable("lease_offers") {
    val carModel = reference("car_model_id", CarModelsTable)
    val company = varchar("company", 50)
    val type = varchar("type", 20)
    val monthlyPayment = integer("monthly_payment")
    val deposit = integer("deposit")
    val contractMonths = integer("contract_months")
    val annualMileage = integer("annual_mileage")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object AuctionsTable : IntIdTable("auctions") {
    val user = reference("user_id", UsersTable)
    val category = reference("category_id", CategoriesTable).nullable()
    val title = varchar("title", 255)
    val description = text("description")
    val specsJson = text("specs_json").nullable()
    val budget = integer("budget").nullable()
    val status = varchar("status", 20)
    val bidCount = integer("bid_count")
    val selectedBidId = integer("selected_bid_id").nullable()
    val expiresAt = timestamp("expires_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object BidsTable : IntIdTable("bids") {
    val auction = reference("auction_id", AuctionsTable)
    val seller = reference("seller_id", SellersTable)
    val price = integer("price")
    val description = varchar("description", 500).nullable()
    val deliveryDays = integer("delivery_days")
    val status = varchar("status", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object CompareItemsTable : IntIdTable("compare_items") {
    val compareKey = varchar("compare_key", 120)
    val product = reference("product_id", ProductsTable)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
