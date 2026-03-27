package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import java.time.Instant

enum class RankingPeriod(
    val apiValue: String,
) {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
}

data class PopularProductRanking(
    val rank: Int,
    val productId: Int,
    val productName: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
    val score: Int,
    val rankChange: Int,
)

data class PopularSearchRanking(
    val rank: Int,
    val keyword: String,
    val searchCount: Int,
    val rankChange: Int,
)

interface RankingRepository {
    fun listPopularProducts(
        categoryId: Int?,
        period: RankingPeriod,
        limit: Int,
    ): List<PopularProductRanking>

    fun listPopularSearches(
        period: RankingPeriod,
        limit: Int,
    ): List<PopularSearchRanking>
}

internal fun RankingPeriod.threshold(now: Instant = Instant.now()): Instant =
    when (this) {
        RankingPeriod.DAILY -> now.minusSeconds(86_400)
        RankingPeriod.WEEKLY -> now.minusSeconds(86_400 * 7L)
        RankingPeriod.MONTHLY -> now.minusSeconds(86_400 * 30L)
    }
