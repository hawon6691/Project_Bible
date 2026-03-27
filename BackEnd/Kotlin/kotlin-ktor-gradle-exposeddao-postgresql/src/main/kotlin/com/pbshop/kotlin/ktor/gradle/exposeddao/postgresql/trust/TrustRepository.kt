package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import java.time.Instant

data class SellerTrustDetailRecord(
    val sellerId: Int,
    val overallScore: Int,
    val grade: String,
    val deliveryScore: Int,
    val priceAccuracy: Int,
    val returnRate: Double,
    val responseTimeHours: Double,
    val reviewScore: Double,
    val orderCount: Int,
    val disputeRate: Double,
    val trend: String,
    val lastUpdatedAt: Instant,
)

data class SellerReviewRecord(
    val id: Int,
    val sellerId: Int,
    val userId: Int,
    val orderId: Int,
    val rating: Int,
    val deliveryRating: Int,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class SellerReviewListResult(
    val items: List<SellerReviewRecord>,
    val totalCount: Int,
)

data class NewSellerReview(
    val orderId: Int,
    val rating: Int,
    val deliveryRating: Int,
    val content: String,
)

data class SellerReviewUpdate(
    val rating: Int? = null,
    val deliveryRating: Int? = null,
    val content: String? = null,
)

interface TrustRepository {
    fun sellerExists(sellerId: Int): Boolean

    fun findTrustDetail(sellerId: Int): SellerTrustDetailRecord?

    fun listReviews(
        sellerId: Int,
        page: Int,
        limit: Int,
        sort: String?,
    ): SellerReviewListResult

    fun findReviewById(id: Int): SellerReviewRecord?

    fun createReview(
        sellerId: Int,
        userId: Int,
        newReview: NewSellerReview,
    ): SellerReviewRecord

    fun updateReview(
        reviewId: Int,
        update: SellerReviewUpdate,
    ): SellerReviewRecord

    fun softDeleteReview(reviewId: Int)

    fun orderExists(orderId: Int): Boolean
}
