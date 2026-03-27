package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderItemsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrdersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellerReviewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellerTrustMetricsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TrustTrend
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant

class ExposedDaoTrustRepository(
    private val databaseFactory: DatabaseFactory,
) : TrustRepository {
    override fun sellerExists(sellerId: Int): Boolean =
        databaseFactory.withTransaction {
            SellersTable.selectAll().where { SellersTable.id eq sellerId }.limit(1).any()
        }

    override fun findTrustDetail(sellerId: Int): SellerTrustDetailRecord? =
        databaseFactory.withTransaction {
            SellerTrustMetricsTable.selectAll()
                .where { SellerTrustMetricsTable.seller eq sellerId }
                .limit(1)
                .firstOrNull()
                ?.let(::toTrustDetail)
        }

    override fun listReviews(
        sellerId: Int,
        page: Int,
        limit: Int,
        sort: String?,
    ): SellerReviewListResult =
        databaseFactory.withTransaction {
            val rows =
                SellerReviewsTable.selectAll()
                    .where { (SellerReviewsTable.seller eq sellerId) and SellerReviewsTable.deletedAt.isNull() }
                    .map(::toReviewRecord)
            val sorted =
                when (sort?.lowercase()) {
                    "rating_asc" -> rows.sortedBy { it.rating }
                    "rating_desc" -> rows.sortedByDescending { it.rating }
                    else -> rows.sortedByDescending { it.createdAt }
                }
            val offset = (page - 1) * limit
            SellerReviewListResult(sorted.drop(offset).take(limit), sorted.size)
        }

    override fun findReviewById(id: Int): SellerReviewRecord? =
        databaseFactory.withTransaction {
            SellerReviewsTable.selectAll()
                .where { (SellerReviewsTable.id eq id) and SellerReviewsTable.deletedAt.isNull() }
                .limit(1)
                .firstOrNull()
                ?.let(::toReviewRecord)
        }

    override fun createReview(
        sellerId: Int,
        userId: Int,
        newReview: NewSellerReview,
    ): SellerReviewRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val id =
                SellerReviewsTable.insertAndGetId { row ->
                    row[seller] = EntityID(sellerId, SellersTable)
                    row[user] = EntityID(userId, UsersTable)
                    row[order] = EntityID(newReview.orderId, OrdersTable)
                    row[rating] = newReview.rating.toShort()
                    row[deliveryRating] = newReview.deliveryRating.toShort()
                    row[content] = newReview.content
                    row[createdAt] = now
                    row[updatedAt] = now
                    row[deletedAt] = null
                }.value
            refreshMetric(sellerId)
            requireNotNull(findReviewById(id))
        }

    override fun updateReview(
        reviewId: Int,
        update: SellerReviewUpdate,
    ): SellerReviewRecord =
        databaseFactory.withTransaction {
            val current =
                SellerReviewsTable.selectAll()
                    .where { (SellerReviewsTable.id eq reviewId) and SellerReviewsTable.deletedAt.isNull() }
                    .limit(1)
                    .first()
            SellerReviewsTable.update({ SellerReviewsTable.id eq reviewId }) { row ->
                row[rating] = (update.rating ?: current[SellerReviewsTable.rating].toInt()).toShort()
                row[deliveryRating] = (update.deliveryRating ?: current[SellerReviewsTable.deliveryRating].toInt()).toShort()
                row[content] = update.content ?: current[SellerReviewsTable.content]
                row[updatedAt] = Instant.now()
            }
            refreshMetric(current[SellerReviewsTable.seller].value)
            requireNotNull(findReviewById(reviewId))
        }

    override fun softDeleteReview(reviewId: Int) {
        databaseFactory.withTransaction {
            val sellerId =
                SellerReviewsTable.selectAll()
                    .where { (SellerReviewsTable.id eq reviewId) and SellerReviewsTable.deletedAt.isNull() }
                    .limit(1)
                    .firstOrNull()
                    ?.get(SellerReviewsTable.seller)
                    ?.value
                    ?: return@withTransaction
            SellerReviewsTable.update({ SellerReviewsTable.id eq reviewId }) {
                it[deletedAt] = Instant.now()
                it[updatedAt] = Instant.now()
            }
            refreshMetric(sellerId)
        }
    }

    override fun orderExists(orderId: Int): Boolean =
        databaseFactory.withTransaction {
            OrdersTable.selectAll().where { OrdersTable.id eq orderId }.limit(1).any()
        }

    private fun refreshMetric(sellerId: Int) {
        val reviews =
            SellerReviewsTable.selectAll()
                .where { (SellerReviewsTable.seller eq sellerId) and SellerReviewsTable.deletedAt.isNull() }
                .toList()
        val avgRating = if (reviews.isEmpty()) 0.0 else reviews.map { it[SellerReviewsTable.rating].toInt() }.average()
        val avgDelivery = if (reviews.isEmpty()) 0.0 else reviews.map { it[SellerReviewsTable.deliveryRating].toInt() }.average()
        val orderCount = OrderItemsTable.selectAll().where { OrderItemsTable.seller eq sellerId }.count().toInt()
        val overallScore = (avgRating * 20).toInt().coerceAtLeast(0)
        val gradeValue =
            when {
                avgRating >= 4.5 -> "A+"
                avgRating >= 4.0 -> "A"
                avgRating >= 3.0 -> "B"
                avgRating > 0 -> "C"
                else -> "D"
            }
        val now = Instant.now()
        val existing =
            SellerTrustMetricsTable.selectAll()
                .where { SellerTrustMetricsTable.seller eq sellerId }
                .limit(1)
                .firstOrNull()
        if (existing == null) {
            SellerTrustMetricsTable.insert { row ->
                row[seller] = EntityID(sellerId, SellersTable)
                row[deliveryScore] = (avgDelivery * 20).toInt()
                row[priceAccuracy] = 90
                row[returnRate] = BigDecimal("2.10")
                row[responseTimeHours] = BigDecimal("1.5")
                row[reviewScore] = BigDecimal(avgRating).setScale(1, java.math.RoundingMode.HALF_UP)
                row[SellerTrustMetricsTable.orderCount] = orderCount
                row[disputeRate] = BigDecimal("0.30")
                row[SellerTrustMetricsTable.overallScore] = overallScore
                row[SellerTrustMetricsTable.grade] = gradeValue
                row[trend] = TrustTrend.STABLE
                row[calculatedAt] = now
                row[createdAt] = now
                row[updatedAt] = now
            }
        } else {
            SellerTrustMetricsTable.update({ SellerTrustMetricsTable.seller eq sellerId }) { row ->
                row[deliveryScore] = (avgDelivery * 20).toInt()
                row[reviewScore] = BigDecimal(avgRating).setScale(1, java.math.RoundingMode.HALF_UP)
                row[SellerTrustMetricsTable.orderCount] = orderCount
                row[SellerTrustMetricsTable.overallScore] = overallScore
                row[SellerTrustMetricsTable.grade] = gradeValue
                row[calculatedAt] = now
                row[updatedAt] = now
            }
        }
    }

    private fun toTrustDetail(row: ResultRow): SellerTrustDetailRecord =
        SellerTrustDetailRecord(
            sellerId = row[SellerTrustMetricsTable.seller].value,
            overallScore = row[SellerTrustMetricsTable.overallScore],
            grade = row[SellerTrustMetricsTable.grade],
            deliveryScore = row[SellerTrustMetricsTable.deliveryScore],
            priceAccuracy = row[SellerTrustMetricsTable.priceAccuracy],
            returnRate = row[SellerTrustMetricsTable.returnRate].toDouble(),
            responseTimeHours = row[SellerTrustMetricsTable.responseTimeHours].toDouble(),
            reviewScore = row[SellerTrustMetricsTable.reviewScore].toDouble(),
            orderCount = row[SellerTrustMetricsTable.orderCount],
            disputeRate = row[SellerTrustMetricsTable.disputeRate].toDouble(),
            trend = row[SellerTrustMetricsTable.trend].name,
            lastUpdatedAt = row[SellerTrustMetricsTable.calculatedAt],
        )

    private fun toReviewRecord(row: ResultRow): SellerReviewRecord =
        SellerReviewRecord(
            id = row[SellerReviewsTable.id].value,
            sellerId = row[SellerReviewsTable.seller].value,
            userId = row[SellerReviewsTable.user].value,
            orderId = row[SellerReviewsTable.order].value,
            rating = row[SellerReviewsTable.rating].toInt(),
            deliveryRating = row[SellerReviewsTable.deliveryRating].toInt(),
            content = row[SellerReviewsTable.content],
            createdAt = row[SellerReviewsTable.createdAt],
            updatedAt = row[SellerReviewsTable.updatedAt],
        )
}
