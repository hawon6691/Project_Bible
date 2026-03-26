package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import java.time.Instant

class InMemoryActivityRepository(
    seededViews: List<ViewHistoryRecord> = emptyList(),
    seededSearches: List<SearchHistoryRecord> = emptyList(),
) : ActivityRepository {
    private val views = linkedMapOf<Int, ViewHistoryRecord>()
    private val searches = linkedMapOf<Int, SearchHistoryRecord>()
    private var nextViewId = 1
    private var nextSearchId = 1

    init {
        seededViews.forEach {
            views[it.id] = it
            nextViewId = maxOf(nextViewId, it.id + 1)
        }
        seededSearches.forEach {
            searches[it.id] = it
            nextSearchId = maxOf(nextSearchId, it.id + 1)
        }
    }

    override fun listViewHistory(
        userId: Int,
        page: Int,
        limit: Int,
    ): ViewHistoryListResult {
        val filtered = views.values.filter { it.userId == userId }.sortedByDescending { it.viewedAt }
        val offset = (page - 1) * limit
        return ViewHistoryListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun clearViewHistory(userId: Int) {
        views.values.filter { it.userId == userId }.map { it.id }.forEach(views::remove)
    }

    override fun listSearchHistories(
        userId: Int,
        page: Int,
        limit: Int,
    ): SearchHistoryListResult {
        val filtered = searches.values.filter { it.userId == userId }.sortedByDescending { it.createdAt }
        val offset = (page - 1) * limit
        return SearchHistoryListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findSearchHistoryById(id: Int): SearchHistoryRecord? = searches[id]

    override fun clearSearchHistories(userId: Int) {
        searches.values.filter { it.userId == userId }.map { it.id }.forEach(searches::remove)
    }

    override fun deleteSearchHistory(id: Int) {
        searches.remove(id)
    }

    companion object {
        fun seeded(): InMemoryActivityRepository {
            val now = Instant.now()
            return InMemoryActivityRepository(
                seededViews =
                    listOf(
                        ViewHistoryRecord(1, 4, 1, "게이밍 노트북 A15", "https://img.example.com/products/a15-thumb.jpg", now.minusSeconds(1800)),
                        ViewHistoryRecord(2, 4, 4, "울트라 와이드 모니터 34", "https://img.example.com/products/monitor-34-thumb.jpg", now.minusSeconds(600)),
                        ViewHistoryRecord(3, 5, 3, "개발자용 노트북 Z14", "https://img.example.com/products/z14-thumb.jpg", now.minusSeconds(3600)),
                    ),
                seededSearches =
                    listOf(
                        SearchHistoryRecord(1, 4, "게이밍 노트북", now.minusSeconds(1200)),
                        SearchHistoryRecord(2, 4, "RTX 5070", now.minusSeconds(900)),
                        SearchHistoryRecord(3, 5, "미니 데스크탑", now.minusSeconds(3000)),
                    ),
            )
        }
    }
}
