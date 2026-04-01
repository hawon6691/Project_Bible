package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class UsedMarketService(
    private val repository: UsedMarketRepository,
) {
    fun productPrice(productId: Int): StubResponse {
        val result =
            repository.productPrice(productId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "중고 시세 정보를 찾을 수 없습니다.")
        return StubResponse(data = summaryPayload(result))
    }

    fun categoryPrices(categoryId: Int, page: Int, limit: Int): StubResponse {
        val normalizedPage = if (page > 0) page else 1
        val normalizedLimit = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20
        val result = repository.categoryPrices(categoryId, normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::summaryPayload),
            meta = mapOf("page" to normalizedPage, "limit" to normalizedLimit, "totalCount" to result.totalCount, "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount - 1) / normalizedLimit) + 1),
        )
    }

    fun estimateBuild(userId: Int, buildId: Int): StubResponse {
        val result =
            repository.estimateBuild(userId, buildId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "PC 빌드를 찾을 수 없습니다.")
        return StubResponse(
            data =
                mapOf(
                    "buildId" to result.buildId,
                    "estimatedPrice" to result.estimatedPrice,
                    "partBreakdown" to result.partBreakdown.map {
                        mapOf(
                            "partType" to it.partType,
                            "productId" to it.productId,
                            "productName" to it.productName,
                            "quantity" to it.quantity,
                            "estimatedPrice" to it.estimatedPrice,
                        )
                    },
                )
        )
    }

    private fun summaryPayload(record: UsedPriceSummaryRecord): Map<String, Any?> =
        mapOf(
            "productId" to record.productId,
            "productName" to record.productName,
            "averagePrice" to record.averagePrice,
            "minPrice" to record.minPrice,
            "maxPrice" to record.maxPrice,
            "sampleCount" to record.sampleCount,
            "trend" to record.trend,
        )
}
