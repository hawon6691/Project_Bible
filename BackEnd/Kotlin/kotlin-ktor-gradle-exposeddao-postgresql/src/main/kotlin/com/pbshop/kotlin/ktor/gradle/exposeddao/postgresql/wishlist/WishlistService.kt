package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class WishlistService(
    private val repository: WishlistRepository,
) {
    fun list(
        userId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listWishlist(userId, queryPage, queryLimit)
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

    fun toggle(
        userId: Int,
        productId: Int,
    ): StubResponse {
        ensureProductExists(productId)
        val existing = repository.findWishlistItem(userId, productId)
        return if (existing == null) {
            repository.createWishlistItem(userId, productId)
            StubResponse(data = mapOf("wishlisted" to true))
        } else {
            repository.deleteWishlistItem(userId, productId)
            StubResponse(data = mapOf("wishlisted" to false))
        }
    }

    fun delete(
        userId: Int,
        productId: Int,
    ): StubResponse {
        repository.deleteWishlistItem(userId, productId)
        return StubResponse(data = mapOf("message" to "Wishlist item removed."))
    }

    private fun ensureProductExists(productId: Int) {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
    }

    private fun payload(record: WishlistItemRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "productId" to record.productId,
            "productName" to record.productName,
            "thumbnailUrl" to record.thumbnailUrl,
            "lowestPrice" to record.lowestPrice,
            "wishlistedAt" to record.wishlistedAt.toString(),
        )
}
