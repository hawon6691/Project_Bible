package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import java.time.LocalDate

data class LowestEverRecord(
    val currentPrice: Int,
    val lowestPrice: Int,
    val lowestDate: LocalDate?,
)

data class UnitPriceRecord(
    val unitPrice: Double,
    val unit: String,
    val quantity: Double,
)

interface AnalyticsRepository {
    fun findLowestEver(productId: Int): LowestEverRecord?

    fun findUnitPrice(productId: Int): UnitPriceRecord?
}
