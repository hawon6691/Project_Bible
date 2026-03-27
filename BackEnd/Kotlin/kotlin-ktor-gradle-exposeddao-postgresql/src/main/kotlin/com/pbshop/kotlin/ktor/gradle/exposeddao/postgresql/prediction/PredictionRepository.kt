package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import java.time.Instant

data class PriceTrendPredictionRecord(
    val productId: Int,
    val trend: String,
    val recommendation: String,
    val expectedLowestPrice: Int,
    val confidence: Double,
    val updatedAt: Instant,
)

interface PredictionRepository {
    fun predictPriceTrend(
        productId: Int,
        days: Int,
    ): PriceTrendPredictionRecord?

    fun productExists(productId: Int): Boolean
}
