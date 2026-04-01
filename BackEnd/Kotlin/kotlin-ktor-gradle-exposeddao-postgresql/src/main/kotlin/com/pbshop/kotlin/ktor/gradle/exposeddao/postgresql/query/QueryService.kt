package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class QueryService(
    private val repository: QueryRepository,
) {
    fun listProducts(query: ProductQueryViewQuery): StubResponse {
        val normalized =
            query.copy(
                page = query.page.coerceAtLeast(1),
                limit = query.limit.coerceIn(1, 100),
            )
        val result = repository.listProducts(normalized)
        return StubResponse(
            data = result.items.map(::payload),
            meta =
                mapOf(
                    "page" to normalized.page,
                    "limit" to normalized.limit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount - 1) / normalized.limit) + 1,
                ),
        )
    }

    fun detail(productId: Int): StubResponse {
        val record =
            repository.findProductDetail(productId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품 읽기 모델을 찾을 수 없습니다.")
        return StubResponse(data = payload(record))
    }

    fun syncProduct(productId: Int): StubResponse {
        val record =
            repository.syncProduct(productId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        return StubResponse(status = HttpStatusCode.Created, data = payload(record))
    }

    fun rebuildAll(): StubResponse = StubResponse(status = HttpStatusCode.Created, data = mapOf("syncedCount" to repository.rebuildAll()))

    private fun payload(record: ProductQueryViewRecord): Map<String, Any?> =
        mapOf(
            "productId" to record.productId,
            "categoryId" to record.categoryId,
            "name" to record.name,
            "thumbnailUrl" to record.thumbnailUrl,
            "status" to record.status,
            "basePrice" to record.basePrice,
            "lowestPrice" to record.lowestPrice,
            "sellerCount" to record.sellerCount,
            "averageRating" to record.averageRating,
            "reviewCount" to record.reviewCount,
            "viewCount" to record.viewCount,
            "popularityScore" to record.popularityScore,
            "syncedAt" to record.syncedAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )
}
