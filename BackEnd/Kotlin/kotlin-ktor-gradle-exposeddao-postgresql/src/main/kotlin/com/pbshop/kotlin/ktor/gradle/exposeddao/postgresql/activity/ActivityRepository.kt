package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import java.time.Instant

data class ViewHistoryRecord(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val thumbnailUrl: String?,
    val viewedAt: Instant,
)

data class ViewHistoryListResult(
    val items: List<ViewHistoryRecord>,
    val totalCount: Int,
)

data class SearchHistoryRecord(
    val id: Int,
    val userId: Int,
    val keyword: String,
    val createdAt: Instant,
)

data class SearchHistoryListResult(
    val items: List<SearchHistoryRecord>,
    val totalCount: Int,
)

interface ActivityRepository {
    fun listViewHistory(
        userId: Int,
        page: Int,
        limit: Int,
    ): ViewHistoryListResult

    fun clearViewHistory(userId: Int)

    fun listSearchHistories(
        userId: Int,
        page: Int,
        limit: Int,
    ): SearchHistoryListResult

    fun findSearchHistoryById(id: Int): SearchHistoryRecord?

    fun clearSearchHistories(userId: Int)

    fun deleteSearchHistory(id: Int)
}
