package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import java.time.Instant

data class ReviewRecord(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val orderId: Int,
    val rating: Int,
    val content: String,
    val isBest: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ReviewListResult(
    val items: List<ReviewRecord>,
    val totalCount: Int,
)

data class NewReview(
    val productId: Int,
    val orderId: Int,
    val rating: Int,
    val content: String,
)

data class ReviewUpdate(
    val rating: Int?,
    val content: String?,
)

interface ReviewRepository {
    fun listProductReviews(
        productId: Int,
        page: Int,
        limit: Int,
    ): ReviewListResult

    fun findReviewById(id: Int): ReviewRecord?

    fun findReviewByUserAndOrder(
        userId: Int,
        orderId: Int,
    ): ReviewRecord?

    fun createReview(
        userId: Int,
        newReview: NewReview,
    ): ReviewRecord

    fun updateReview(
        reviewId: Int,
        update: ReviewUpdate,
    ): ReviewRecord

    fun deleteReview(reviewId: Int)
}
