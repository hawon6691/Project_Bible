package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderRepository
import io.ktor.http.HttpStatusCode

class ReviewService(
    private val repository: ReviewRepository,
    private val orderRepository: OrderRepository,
) {
    fun listProductReviews(
        productId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listProductReviews(productId, queryPage, queryLimit)
        return StubResponse(
            data = result.items.map(::payload),
            meta =
                mapOf(
                    "page" to queryPage,
                    "limit" to queryLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + queryLimit - 1) / queryLimit),
                ),
        )
    }

    fun createReview(
        userId: Int,
        productId: Int,
        request: ReviewRequest,
    ): StubResponse {
        validate(request.rating, request.content)
        val order =
            orderRepository.findOrderDetailById(userId, request.orderId)
                ?: throw PbShopException(HttpStatusCode.BadRequest, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다.")
        if (order.items.none { it.productId == productId }) {
            throw PbShopException(HttpStatusCode.BadRequest, "ORDER_PRODUCT_MISMATCH", "해당 주문에 포함되지 않은 상품입니다.")
        }
        if (repository.findReviewByUserAndOrder(userId, request.orderId) != null) {
            throw PbShopException(HttpStatusCode.BadRequest, "REVIEW_ALREADY_EXISTS", "이미 해당 주문에 대한 리뷰가 존재합니다.")
        }
        val created =
            repository.createReview(
                userId,
                NewReview(
                    productId = productId,
                    orderId = request.orderId,
                    rating = request.rating,
                    content = request.content.trim(),
                ),
            )
        return StubResponse(
            status = HttpStatusCode.Created,
            data = payload(created) + mapOf("awardedPoint" to 500),
        )
    }

    fun updateReview(
        userId: Int,
        reviewId: Int,
        request: ReviewUpdateRequest,
    ): StubResponse {
        if (request.rating == null && request.content == null) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 값이 없습니다.")
        }
        request.rating?.let { validate(it, request.content ?: "placeholder") }
        request.content?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.") }
        val existing = requireReview(reviewId)
        if (existing.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "REVIEW_FORBIDDEN", "본인의 리뷰만 수정할 수 있습니다.")
        }
        val updated = repository.updateReview(reviewId, ReviewUpdate(request.rating, request.content?.trim()))
        return StubResponse(data = payload(updated))
    }

    fun deleteReview(
        userId: Int?,
        isAdmin: Boolean,
        reviewId: Int,
    ): StubResponse {
        val existing = requireReview(reviewId)
        if (!isAdmin && existing.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "REVIEW_FORBIDDEN", "본인의 리뷰만 삭제할 수 있습니다.")
        }
        repository.deleteReview(reviewId)
        return StubResponse(data = mapOf("message" to "Review deleted."))
    }

    private fun requireReview(reviewId: Int): ReviewRecord =
        repository.findReviewById(reviewId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다.")

    private fun validate(
        rating: Int,
        content: String,
    ) {
        if (rating !in 1..5) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "rating은 1 이상 5 이하여야 합니다.")
        }
        if (content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.")
        }
    }

    private fun payload(record: ReviewRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "userId" to record.userId,
            "productId" to record.productId,
            "orderId" to record.orderId,
            "rating" to record.rating,
            "content" to record.content,
            "isBest" to record.isBest,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )
}
