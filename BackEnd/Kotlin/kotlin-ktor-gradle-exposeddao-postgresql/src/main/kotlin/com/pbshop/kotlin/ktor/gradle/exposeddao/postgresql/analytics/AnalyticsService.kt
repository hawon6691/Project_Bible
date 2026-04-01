package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class AnalyticsService(
    private val repository: AnalyticsRepository,
) {
    fun lowestEver(productId: Int): StubResponse {
        val record =
            repository.findLowestEver(productId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "상품 가격 분석 정보를 찾을 수 없습니다.")
        return StubResponse(
            data = mapOf(
                "isLowestEver" to (record.currentPrice <= record.lowestPrice),
                "currentPrice" to record.currentPrice,
                "lowestPrice" to record.lowestPrice,
                "lowestDate" to record.lowestDate?.toString(),
            )
        )
    }

    fun unitPrice(productId: Int): StubResponse {
        val record =
            repository.findUnitPrice(productId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "단가 계산 정보를 찾을 수 없습니다.")
        return StubResponse(
            data = mapOf(
                "unitPrice" to record.unitPrice,
                "unit" to record.unit,
                "quantity" to record.quantity,
            )
        )
    }
}
