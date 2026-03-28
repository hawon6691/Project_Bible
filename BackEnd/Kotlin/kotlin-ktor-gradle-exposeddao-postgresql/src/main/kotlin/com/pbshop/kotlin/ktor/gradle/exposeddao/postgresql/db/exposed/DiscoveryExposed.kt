package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

enum class RecommendationType {
    TODAY,
    EDITOR_PICK,
    NEW_ARRIVAL,
}

enum class CrawlerLogStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
}

enum class TrustTrend {
    IMPROVING,
    STABLE,
    DECLINING,
}

object RecommendationsTable : IntIdTable("recommendations") {
    val product = reference("product_id", ProductsTable)
    val type = pgEnum<RecommendationType>("type", "recommendation_type")
    val sortOrder = integer("sort_order")
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val createdAt = timestamp("created_at")
}

object DealsTable : IntIdTable("deals") {
    val product = reference("product_id", ProductsTable)
    val title = varchar("title", 120)
    val description = text("description").nullable()
    val discountRate = integer("discount_rate")
    val startAt = timestamp("start_at")
    val endAt = timestamp("end_at")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object DealProductsTable : IntIdTable("deal_products") {
    val deal = reference("deal_id", DealsTable)
    val product = reference("product_id", ProductsTable)
    val dealPrice = integer("deal_price")
    val stock = integer("stock")
    val soldCount = integer("sold_count")
    val createdAt = timestamp("created_at")
}

object SearchLogsTable : LongIdTable("search_logs") {
    val user = reference("user_id", UsersTable).nullable()
    val keyword = varchar("keyword", 200)
    val resultCount = integer("result_count")
    val categoryId = integer("category_id").nullable()
    val filters = text("filters").nullable()
    val responseTimeMs = integer("response_time_ms")
    val searchedAt = timestamp("searched_at")
}

object SearchSynonymsTable : IntIdTable("search_synonyms") {
    val word = varchar("word", 100).uniqueIndex()
    val synonyms = text("synonyms")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object SearchIndexOutboxTable : IntIdTable("search_index_outbox") {
    val eventType = varchar("event_type", 30)
    val status = varchar("status", 20)
    val aggregateId = integer("aggregate_id")
    val attemptCount = integer("attempt_count")
    val lastError = varchar("last_error", 500).nullable()
    val processedAt = timestamp("processed_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object CrawlerJobsTable : IntIdTable("crawler_jobs") {
    val seller = reference("seller_id", SellersTable)
    val name = varchar("name", 100)
    val cronExpression = varchar("cron_expression", 100).nullable()
    val collectPrice = bool("collect_price")
    val collectSpec = bool("collect_spec")
    val detectAnomaly = bool("detect_anomaly")
    val isActive = bool("is_active")
    val lastTriggeredAt = timestamp("last_triggered_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object CrawlerLogsTable : LongIdTable("crawler_logs") {
    val job = reference("job_id", CrawlerJobsTable)
    val status = pgEnum<CrawlerLogStatus>("status", "crawler_log_status")
    val startedAt = timestamp("started_at")
    val finishedAt = timestamp("finished_at").nullable()
    val durationMs = integer("duration_ms").nullable()
    val itemsProcessed = integer("items_processed")
    val itemsCreated = integer("items_created")
    val itemsUpdated = integer("items_updated")
    val itemsFailed = integer("items_failed")
    val errorMessage = text("error_message").nullable()
    val createdAt = timestamp("created_at")
}

object PricePredictionsTable : IntIdTable("price_predictions") {
    val product = reference("product_id", ProductsTable)
    val predictionDate = date("prediction_date")
    val predictedPrice = integer("predicted_price")
    val confidence = decimal("confidence", 4, 2)
    val recommendation = varchar("recommendation", 20)
    val createdAt = timestamp("created_at")
}

object SellerTrustMetricsTable : IntIdTable("seller_trust_metrics") {
    val seller = reference("seller_id", SellersTable).uniqueIndex()
    val deliveryScore = integer("delivery_score")
    val priceAccuracy = integer("price_accuracy")
    val returnRate = decimal("return_rate", 5, 2)
    val responseTimeHours = decimal("response_time_hours", 5, 1)
    val reviewScore = decimal("review_score", 2, 1)
    val orderCount = integer("order_count")
    val disputeRate = decimal("dispute_rate", 5, 2)
    val overallScore = integer("overall_score")
    val grade = varchar("grade", 2)
    val trend = pgEnum<TrustTrend>("trend", "trust_trend")
    val calculatedAt = timestamp("calculated_at")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object SellerReviewsTable : IntIdTable("seller_reviews") {
    val seller = reference("seller_id", SellersTable)
    val user = reference("user_id", UsersTable)
    val order = reference("order_id", OrdersTable)
    val rating = short("rating")
    val deliveryRating = short("delivery_rating")
    val content = text("content")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}
