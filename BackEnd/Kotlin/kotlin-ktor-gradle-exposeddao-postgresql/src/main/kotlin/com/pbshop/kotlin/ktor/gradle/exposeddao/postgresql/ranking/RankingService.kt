package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class RankingService(
    private val repository: RankingRepository,
) {
    fun popularProducts(
        categoryId: Int?,
        period: String?,
        limit: Int?,
    ): StubResponse {
        val queryLimit = (limit ?: 20).coerceIn(1, 100)
        val rankingPeriod = parsePeriod(period)
        val rankings = repository.listPopularProducts(categoryId, rankingPeriod, queryLimit)
        return StubResponse(
            data =
                rankings.map {
                    mapOf(
                        "rank" to it.rank,
                        "product" to
                            mapOf(
                                "id" to it.productId,
                                "name" to it.productName,
                                "thumbnailUrl" to it.thumbnailUrl,
                                "lowestPrice" to it.lowestPrice,
                            ),
                        "score" to it.score,
                        "rankChange" to it.rankChange,
                    )
                },
        )
    }

    fun popularSearches(
        period: String?,
        limit: Int?,
    ): StubResponse {
        val queryLimit = (limit ?: 10).coerceIn(1, 100)
        val rankingPeriod = parsePeriod(period)
        val rankings = repository.listPopularSearches(rankingPeriod, queryLimit)
        return StubResponse(
            data =
                rankings.map {
                    mapOf(
                        "rank" to it.rank,
                        "keyword" to it.keyword,
                        "searchCount" to it.searchCount,
                        "rankChange" to it.rankChange,
                    )
                },
        )
    }

    private fun parsePeriod(value: String?): RankingPeriod =
        if (value.isNullOrBlank()) {
            RankingPeriod.DAILY
        } else {
            RankingPeriod.entries.firstOrNull { it.apiValue == value.trim().lowercase() }
                ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 period 값입니다.")
        }
}
