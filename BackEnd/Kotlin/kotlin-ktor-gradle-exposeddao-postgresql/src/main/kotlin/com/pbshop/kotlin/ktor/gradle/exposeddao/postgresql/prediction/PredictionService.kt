package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class PredictionService(
    private val repository: PredictionRepository,
) {
    fun priceTrend(
        productId: Int,
        days: Int?,
    ): StubResponse {
        val queryDays = (days ?: 30).coerceIn(1, 365)
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        val prediction =
            repository.predictPriceTrend(productId, queryDays)
                ?: throw PbShopException(HttpStatusCode.NotFound, "PREDICTION_NOT_FOUND", "예측 정보를 찾을 수 없습니다.")
        return StubResponse(
            data =
                mapOf(
                    "productId" to prediction.productId,
                    "trend" to prediction.trend,
                    "recommendation" to prediction.recommendation,
                    "expectedLowestPrice" to prediction.expectedLowestPrice,
                    "confidence" to prediction.confidence,
                    "updatedAt" to prediction.updatedAt.toString(),
                ),
        )
    }
}
