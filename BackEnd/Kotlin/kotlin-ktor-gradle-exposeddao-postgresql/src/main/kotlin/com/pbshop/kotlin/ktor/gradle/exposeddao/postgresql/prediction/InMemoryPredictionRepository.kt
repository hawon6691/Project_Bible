package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import java.time.Instant

class InMemoryPredictionRepository private constructor() : PredictionRepository {
    private val predictions =
        mapOf(
            1 to PriceTrendPredictionRecord(1, "FALLING", "BUY_SOON", 1450000, 0.84, Instant.now().minusSeconds(3600)),
            2 to PriceTrendPredictionRecord(2, "STABLE", "WATCH", 1820000, 0.71, Instant.now().minusSeconds(7200)),
        )

    override fun predictPriceTrend(
        productId: Int,
        days: Int,
    ): PriceTrendPredictionRecord? = predictions[productId]

    override fun productExists(productId: Int): Boolean = productId in setOf(1, 2, 3)

    companion object {
        fun seeded(): InMemoryPredictionRepository = InMemoryPredictionRepository()
    }
}
