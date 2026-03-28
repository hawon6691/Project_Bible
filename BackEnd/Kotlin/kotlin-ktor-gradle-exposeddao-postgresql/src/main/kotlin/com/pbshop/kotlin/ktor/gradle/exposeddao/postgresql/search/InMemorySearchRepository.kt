package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemorySearchRepository private constructor() : SearchRepository {
    private data class SearchOutboxEntry(
        val id: Int,
        val eventType: String,
        var status: String,
        val aggregateId: Int,
        var attemptCount: Int,
        var lastError: String?,
        var processedAt: Instant?,
        val createdAt: Instant,
        var updatedAt: Instant,
    )

    private val recentByUser = ConcurrentHashMap<Int, MutableList<SearchRecentKeywordRecord>>()
    private val preferenceByUser = ConcurrentHashMap<Int, Boolean>()
    private val outboxEntries = mutableListOf<SearchOutboxEntry>()
    private var nextRecentId = 3
    private var nextOutboxId = 3
    private var weights = SearchWeightConfig(1.0, 1.5, 0.8)
    private var indexStatus = SearchIndexStatusRecord("HEALTHY", 1200, Instant.now().minusSeconds(300))

    init {
        recentByUser[4] =
            mutableListOf(
                SearchRecentKeywordRecord(1, "갤럭시북", Instant.now().minusSeconds(3_600)),
                SearchRecentKeywordRecord(2, "7800x3d", Instant.now().minusSeconds(1_800)),
            )
        preferenceByUser[4] = true
        outboxEntries +=
            SearchOutboxEntry(
                id = 1,
                eventType = "PRODUCT_REINDEX",
                status = "FAILED",
                aggregateId = 1,
                attemptCount = 2,
                lastError = "index writer timeout",
                processedAt = null,
                createdAt = Instant.now().minusSeconds(1200),
                updatedAt = Instant.now().minusSeconds(900),
            )
        outboxEntries +=
            SearchOutboxEntry(
                id = 2,
                eventType = "FULL_REINDEX",
                status = "COMPLETED",
                aggregateId = 0,
                attemptCount = 1,
                lastError = null,
                processedAt = Instant.now().minusSeconds(600),
                createdAt = Instant.now().minusSeconds(1800),
                updatedAt = Instant.now().minusSeconds(600),
            )
    }

    override fun search(query: SearchQuery): SearchResultRecord =
        SearchResultRecord(
            hits =
                listOf(
                    SearchHitRecord(1, "게이밍 노트북 A15", "https://img.example.com/products/1-thumb.jpg", 1490000, "노트북", 9.8),
                    SearchHitRecord(2, "크리에이터북 16", "https://img.example.com/products/2-thumb.jpg", 1890000, "노트북", 8.7),
                ),
            categories = listOf(SearchFacetCategory(2, "노트북", 12)),
            suggestions = listOf("갤럭시북", "갤럭시북4"),
            totalCount = 2,
        )

    override fun autocomplete(query: String): Map<String, List<Map<String, Any?>>> =
        mapOf(
            "keywords" to listOf(mapOf("value" to "갤럭시북4 프로"), mapOf("value" to "갤럭시북4")),
            "products" to listOf(mapOf("id" to 1, "name" to "게이밍 노트북 A15", "thumbnailUrl" to "https://img.example.com/products/1-thumb.jpg", "lowestPrice" to 1490000)),
            "categories" to listOf(mapOf("id" to 2, "name" to "노트북")),
        )

    override fun popularKeywords(limit: Int): List<Map<String, Any?>> =
        listOf(
            mapOf("rank" to 1, "keyword" to "갤럭시북", "searchCount" to 5230, "rankChange" to 0),
            mapOf("rank" to 2, "keyword" to "맥북 프로", "searchCount" to 4120, "rankChange" to 1),
        ).take(limit)

    override fun saveRecentKeyword(
        userId: Int,
        keyword: String,
    ): SearchRecentKeywordRecord {
        val created = SearchRecentKeywordRecord(nextRecentId++, keyword, Instant.now())
        val items = recentByUser.getOrPut(userId) { mutableListOf() }
        items.removeIf { it.keyword.equals(keyword, true) }
        items.add(0, created)
        return created
    }

    override fun listRecentKeywords(userId: Int): List<SearchRecentKeywordRecord> = recentByUser[userId]?.toList() ?: emptyList()

    override fun removeRecentKeyword(
        userId: Int,
        recentId: Int,
    ) {
        recentByUser[userId]?.removeIf { it.id == recentId }
    }

    override fun clearRecentKeywords(userId: Int) {
        recentByUser[userId]?.clear()
    }

    override fun getPreference(userId: Int): Boolean = preferenceByUser[userId] ?: true

    override fun updatePreference(
        userId: Int,
        saveRecentSearches: Boolean,
    ): Boolean {
        preferenceByUser[userId] = saveRecentSearches
        return saveRecentSearches
    }

    override fun getWeights(): SearchWeightConfig = weights

    override fun updateWeights(
        nameWeight: Double,
        keywordWeight: Double,
        clickWeight: Double,
    ): SearchWeightConfig {
        weights = SearchWeightConfig(nameWeight, keywordWeight, clickWeight)
        return weights
    }

    override fun getIndexStatus(): SearchIndexStatusRecord = indexStatus

    override fun reindexAllProducts(): Pair<String, Boolean> {
        val now = Instant.now()
        indexStatus = indexStatus.copy(lastIndexedAt = now)
        outboxEntries +=
            SearchOutboxEntry(
                id = nextOutboxId++,
                eventType = "FULL_REINDEX",
                status = "PENDING",
                aggregateId = 0,
                attemptCount = 0,
                lastError = null,
                processedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        return "Reindex queued." to true
    }

    override fun reindexProduct(productId: Int): Pair<String, Int> {
        val now = Instant.now()
        indexStatus = indexStatus.copy(lastIndexedAt = now)
        outboxEntries +=
            SearchOutboxEntry(
                id = nextOutboxId++,
                eventType = "PRODUCT_REINDEX",
                status = "PENDING",
                aggregateId = productId,
                attemptCount = 0,
                lastError = null,
                processedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        return "Product reindex queued." to productId
    }

    override fun getOutboxSummary(): SearchOutboxSummaryRecord =
        SearchOutboxSummaryRecord(
            total = outboxEntries.size,
            pending = outboxEntries.count { it.status == "PENDING" },
            processing = outboxEntries.count { it.status == "PROCESSING" },
            completed = outboxEntries.count { it.status == "COMPLETED" },
            failed = outboxEntries.count { it.status == "FAILED" },
        )

    override fun requeueFailed(limit: Int): Int {
        val now = Instant.now()
        val targets = outboxEntries.filter { it.status == "FAILED" }.sortedBy { it.updatedAt }.take(limit)
        targets.forEach {
            it.status = "PENDING"
            it.lastError = null
            it.processedAt = null
            it.updatedAt = now
        }
        return targets.size
    }

    companion object {
        fun seeded(): InMemorySearchRepository = InMemorySearchRepository()
    }
}
