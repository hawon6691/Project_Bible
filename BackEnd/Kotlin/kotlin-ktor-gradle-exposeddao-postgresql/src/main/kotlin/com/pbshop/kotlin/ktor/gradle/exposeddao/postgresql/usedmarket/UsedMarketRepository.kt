package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

data class UsedPriceSummaryRecord(
    val productId: Int,
    val productName: String,
    val averagePrice: Int,
    val minPrice: Int,
    val maxPrice: Int,
    val sampleCount: Int,
    val trend: String,
)

data class UsedPricePageResult(
    val items: List<UsedPriceSummaryRecord>,
    val totalCount: Int,
)

data class UsedBuildEstimatePartRecord(
    val partType: String,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val estimatedPrice: Int,
)

data class UsedBuildEstimateRecord(
    val buildId: Int,
    val estimatedPrice: Int,
    val partBreakdown: List<UsedBuildEstimatePartRecord>,
)

interface UsedMarketRepository {
    fun productPrice(productId: Int): UsedPriceSummaryRecord?

    fun categoryPrices(categoryId: Int, page: Int, limit: Int): UsedPricePageResult

    fun estimateBuild(userId: Int, buildId: Int): UsedBuildEstimateRecord?
}
