package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class ActivityService(
    private val repository: ActivityRepository,
) {
    fun views(
        userId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = page.coerceAtLeast(1)
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listViewHistory(userId, queryPage, queryLimit)
        return StubResponse(
            data =
                result.items.map {
                    mapOf(
                        "id" to it.id,
                        "productId" to it.productId,
                        "productName" to it.productName,
                        "thumbnailUrl" to it.thumbnailUrl,
                        "viewedAt" to it.viewedAt.toString(),
                    )
                },
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun clearViews(userId: Int): StubResponse {
        repository.clearViewHistory(userId)
        return StubResponse(data = mapOf("message" to "View history cleared."))
    }

    fun searches(
        userId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = page.coerceAtLeast(1)
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listSearchHistories(userId, queryPage, queryLimit)
        return StubResponse(
            data =
                result.items.map {
                    mapOf(
                        "id" to it.id,
                        "keyword" to it.keyword,
                        "createdAt" to it.createdAt.toString(),
                    )
                },
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun clearSearches(userId: Int): StubResponse {
        repository.clearSearchHistories(userId)
        return StubResponse(data = mapOf("message" to "Search history cleared."))
    }

    fun deleteSearch(
        userId: Int,
        searchId: Int,
    ): StubResponse {
        val existing =
            repository.findSearchHistoryById(searchId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "SEARCH_HISTORY_NOT_FOUND", "검색 기록을 찾을 수 없습니다.")
        if (existing.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "SEARCH_HISTORY_FORBIDDEN", "본인의 검색 기록만 삭제할 수 있습니다.")
        }
        repository.deleteSearchHistory(searchId)
        return StubResponse(data = mapOf("message" to "Search history entry deleted."))
    }

    private fun pageMeta(
        page: Int,
        limit: Int,
        totalCount: Int,
    ): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount + limit - 1) / limit),
        )
}
