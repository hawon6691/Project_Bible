package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class InMemoryTrustRepository private constructor() : TrustRepository {
    private val metrics = linkedMapOf<Int, SellerTrustDetailRecord>()
    private val reviews = linkedMapOf<Int, SellerReviewRecord>()
    private val nextReviewId = AtomicInteger(2)

    init {
        val now = Instant.now()
        metrics[1] =
            SellerTrustDetailRecord(
                sellerId = 1,
                overallScore = 95,
                grade = "A+",
                deliveryScore = 97,
                priceAccuracy = 93,
                returnRate = 2.1,
                responseTimeHours = 1.5,
                reviewScore = 4.8,
                orderCount = 152340,
                disputeRate = 0.3,
                trend = "STABLE",
                lastUpdatedAt = now.minusSeconds(3600),
            )
        reviews[1] =
            SellerReviewRecord(
                id = 1,
                sellerId = 1,
                userId = 4,
                orderId = 1,
                rating = 5,
                deliveryRating = 5,
                content = "빠른 배송과 정확한 상품!",
                createdAt = now.minusSeconds(7200),
                updatedAt = now.minusSeconds(7200),
            )
    }

    override fun sellerExists(sellerId: Int): Boolean = sellerId in setOf(1, 2, 3)

    override fun findTrustDetail(sellerId: Int): SellerTrustDetailRecord? = metrics[sellerId]

    override fun listReviews(
        sellerId: Int,
        page: Int,
        limit: Int,
        sort: String?,
    ): SellerReviewListResult {
        val sorted =
            reviews.values.filter { it.sellerId == sellerId }
                .sortedWith(
                    when (sort?.lowercase()) {
                        "rating_asc" -> compareBy { it.rating }
                        "rating_desc" -> compareByDescending { it.rating }
                        else -> compareByDescending<SellerReviewRecord> { it.createdAt }
                    },
                )
        val offset = (page - 1) * limit
        return SellerReviewListResult(sorted.drop(offset).take(limit), sorted.size)
    }

    override fun findReviewById(id: Int): SellerReviewRecord? = reviews[id]

    override fun createReview(
        sellerId: Int,
        userId: Int,
        newReview: NewSellerReview,
    ): SellerReviewRecord {
        val now = Instant.now()
        val created =
            SellerReviewRecord(
                id = nextReviewId.getAndIncrement(),
                sellerId = sellerId,
                userId = userId,
                orderId = newReview.orderId,
                rating = newReview.rating,
                deliveryRating = newReview.deliveryRating,
                content = newReview.content,
                createdAt = now,
                updatedAt = now,
            )
        reviews[created.id] = created
        refreshMetric(sellerId)
        return created
    }

    override fun updateReview(
        reviewId: Int,
        update: SellerReviewUpdate,
    ): SellerReviewRecord {
        val current = requireNotNull(reviews[reviewId])
        val updated =
            current.copy(
                rating = update.rating ?: current.rating,
                deliveryRating = update.deliveryRating ?: current.deliveryRating,
                content = update.content ?: current.content,
                updatedAt = Instant.now(),
            )
        reviews[reviewId] = updated
        refreshMetric(updated.sellerId)
        return updated
    }

    override fun softDeleteReview(reviewId: Int) {
        val removed = reviews.remove(reviewId) ?: return
        refreshMetric(removed.sellerId)
    }

    override fun orderExists(orderId: Int): Boolean = orderId in setOf(1, 2, 3)

    private fun refreshMetric(sellerId: Int) {
        val sellerReviews = reviews.values.filter { it.sellerId == sellerId }
        val avgRating = if (sellerReviews.isEmpty()) 0.0 else sellerReviews.map { it.rating }.average()
        val avgDelivery = if (sellerReviews.isEmpty()) 0.0 else sellerReviews.map { it.deliveryRating }.average()
        metrics[sellerId] =
            (metrics[sellerId] ?: metrics.getValue(1)).copy(
                sellerId = sellerId,
                reviewScore = avgRating,
                deliveryScore = avgDelivery.toInt().coerceAtLeast(0) * 20,
                overallScore = (avgRating * 20).toInt().coerceAtLeast(0),
                grade = when {
                    avgRating >= 4.5 -> "A+"
                    avgRating >= 4.0 -> "A"
                    avgRating >= 3.0 -> "B"
                    avgRating > 0 -> "C"
                    else -> "D"
                },
                lastUpdatedAt = Instant.now(),
            )
    }

    companion object {
        fun seeded(): InMemoryTrustRepository = InMemoryTrustRepository()
    }
}
