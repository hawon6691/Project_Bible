package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class TrustService(
    private val repository: TrustRepository,
) {
    fun detail(sellerId: Int): StubResponse =
        StubResponse(data = trustPayload(requireTrust(sellerId)))

    fun reviews(
        sellerId: Int,
        page: Int?,
        limit: Int?,
        sort: String?,
    ): StubResponse {
        ensureSellerExists(sellerId)
        val queryPage = (page ?: 1).coerceAtLeast(1)
        val queryLimit = (limit ?: 20).coerceIn(1, 100)
        val result = repository.listReviews(sellerId, queryPage, queryLimit, sort)
        return StubResponse(
            data = result.items.map(::reviewPayload),
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun create(
        sellerId: Int,
        userId: Int,
        request: SellerReviewCreateRequest,
    ): StubResponse {
        validateReview(request.rating, request.deliveryRating, request.content)
        ensureSellerExists(sellerId)
        if (!repository.orderExists(request.orderId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "ORDER_NOT_FOUND", "주문 정보를 찾을 수 없습니다.")
        }
        val created =
            repository.createReview(
                sellerId,
                userId,
                NewSellerReview(
                    orderId = request.orderId,
                    rating = request.rating,
                    deliveryRating = request.deliveryRating,
                    content = request.content.trim(),
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = reviewPayload(created))
    }

    fun update(
        reviewId: Int,
        userId: Int,
        isAdmin: Boolean,
        request: SellerReviewUpdateRequest,
    ): StubResponse {
        val existing = requireReview(reviewId)
        if (!isAdmin && existing.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "SELLER_REVIEW_FORBIDDEN", "본인 리뷰만 수정할 수 있습니다.")
        }
        request.rating?.let { if (it !in 1..5) invalidRating() }
        request.deliveryRating?.let { if (it !in 1..5) invalidDeliveryRating() }
        request.content?.let { if (it.trim().isBlank()) invalidContent() }
        val updated =
            repository.updateReview(
                reviewId,
                SellerReviewUpdate(
                    rating = request.rating,
                    deliveryRating = request.deliveryRating,
                    content = request.content?.trim(),
                ),
            )
        return StubResponse(data = reviewPayload(updated))
    }

    fun delete(
        reviewId: Int,
        userId: Int,
        isAdmin: Boolean,
    ): StubResponse {
        val existing = requireReview(reviewId)
        if (!isAdmin && existing.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "SELLER_REVIEW_FORBIDDEN", "본인 리뷰만 삭제할 수 있습니다.")
        }
        repository.softDeleteReview(reviewId)
        return StubResponse(data = mapOf("message" to "Seller review deleted."))
    }

    private fun validateReview(
        rating: Int,
        deliveryRating: Int,
        content: String,
    ) {
        if (rating !in 1..5) invalidRating()
        if (deliveryRating !in 1..5) invalidDeliveryRating()
        if (content.trim().isBlank()) invalidContent()
    }

    private fun invalidRating(): Nothing =
        throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "rating은 1부터 5 사이여야 합니다.")

    private fun invalidDeliveryRating(): Nothing =
        throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "deliveryRating은 1부터 5 사이여야 합니다.")

    private fun invalidContent(): Nothing =
        throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.")

    private fun requireTrust(sellerId: Int): SellerTrustDetailRecord {
        ensureSellerExists(sellerId)
        return repository.findTrustDetail(sellerId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "TRUST_NOT_FOUND", "판매처 신뢰도 정보를 찾을 수 없습니다.")
    }

    private fun requireReview(reviewId: Int): SellerReviewRecord =
        repository.findReviewById(reviewId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "SELLER_REVIEW_NOT_FOUND", "판매처 리뷰를 찾을 수 없습니다.")

    private fun ensureSellerExists(sellerId: Int) {
        if (!repository.sellerExists(sellerId)) {
            throw PbShopException(HttpStatusCode.NotFound, "SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.")
        }
    }

    private fun trustPayload(record: SellerTrustDetailRecord): Map<String, Any?> =
        mapOf(
            "sellerId" to record.sellerId,
            "overallScore" to record.overallScore,
            "grade" to record.grade,
            "breakdown" to
                mapOf(
                    "deliveryScore" to record.deliveryScore,
                    "priceAccuracy" to record.priceAccuracy,
                    "returnRate" to record.returnRate,
                    "responseTime" to record.responseTimeHours,
                    "reviewScore" to record.reviewScore,
                    "orderCount" to record.orderCount,
                    "disputeRate" to record.disputeRate,
                ),
            "trend" to record.trend,
            "lastUpdatedAt" to record.lastUpdatedAt.toString(),
        )

    private fun reviewPayload(record: SellerReviewRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "sellerId" to record.sellerId,
            "userId" to record.userId,
            "orderId" to record.orderId,
            "rating" to record.rating,
            "deliveryRating" to record.deliveryRating,
            "content" to record.content,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun pageMeta(
        page: Int,
        limit: Int,
        totalCount: Int,
    ): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount + limit - 1) / limit),
        )
}
