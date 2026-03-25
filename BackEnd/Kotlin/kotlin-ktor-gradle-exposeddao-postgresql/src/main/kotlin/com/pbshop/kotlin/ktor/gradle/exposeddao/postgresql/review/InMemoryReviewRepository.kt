package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import java.time.Instant

class InMemoryReviewRepository(
    seededReviews: List<ReviewRecord> = emptyList(),
) : ReviewRepository {
    private val reviews = linkedMapOf<Int, ReviewRecord>()
    private var nextId = 1

    init {
        seededReviews.forEach {
            reviews[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun listProductReviews(
        productId: Int,
        page: Int,
        limit: Int,
    ): ReviewListResult {
        val filtered = reviews.values.filter { it.productId == productId }.sortedByDescending { it.id }
        val offset = (page - 1) * limit
        return ReviewListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findReviewById(id: Int): ReviewRecord? = reviews[id]

    override fun findReviewByUserAndOrder(
        userId: Int,
        orderId: Int,
    ): ReviewRecord? = reviews.values.firstOrNull { it.userId == userId && it.orderId == orderId }

    override fun createReview(
        userId: Int,
        newReview: NewReview,
    ): ReviewRecord {
        val now = Instant.now()
        val created =
            ReviewRecord(
                id = nextId++,
                userId = userId,
                productId = newReview.productId,
                orderId = newReview.orderId,
                rating = newReview.rating,
                content = newReview.content,
                isBest = false,
                createdAt = now,
                updatedAt = now,
            )
        reviews[created.id] = created
        return created
    }

    override fun updateReview(
        reviewId: Int,
        update: ReviewUpdate,
    ): ReviewRecord {
        val current = requireNotNull(reviews[reviewId]) { "Review $reviewId not found" }
        val updated =
            current.copy(
                rating = update.rating ?: current.rating,
                content = update.content ?: current.content,
                updatedAt = Instant.now(),
            )
        reviews[reviewId] = updated
        return updated
    }

    override fun deleteReview(reviewId: Int) {
        reviews.remove(reviewId)
    }

    companion object {
        fun seeded(): InMemoryReviewRepository =
            InMemoryReviewRepository(
                seededReviews =
                    listOf(
                        ReviewRecord(1, 4, 1, 1, 5, "성능이 매우 좋고 발열 관리도 괜찮습니다.", true, Instant.now().minusSeconds(86400), Instant.now().minusSeconds(86400)),
                        ReviewRecord(2, 5, 3, 2, 4, "개발용으로 충분한 성능입니다.", false, Instant.now().minusSeconds(7200), Instant.now().minusSeconds(7200)),
                    ),
            )
    }
}
