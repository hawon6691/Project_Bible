package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import java.time.Instant

data class SearchHitRecord(
    val id: Int,
    val name: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
    val categoryName: String?,
    val score: Double,
)

data class SearchFacetCategory(
    val id: Int,
    val name: String,
    val count: Int,
)

data class SearchResultRecord(
    val hits: List<SearchHitRecord>,
    val categories: List<SearchFacetCategory>,
    val suggestions: List<String>,
    val totalCount: Int,
)

data class SearchRecentKeywordRecord(
    val id: Int,
    val keyword: String,
    val createdAt: Instant,
)

data class SearchWeightConfig(
    val nameWeight: Double,
    val keywordWeight: Double,
    val clickWeight: Double,
)

data class SearchIndexStatusRecord(
    val status: String,
    val documents: Int,
    val lastIndexedAt: Instant?,
)

data class SearchOutboxSummaryRecord(
    val total: Int,
    val pending: Int,
    val processing: Int,
    val completed: Int,
    val failed: Int,
)

data class SearchQuery(
    val q: String,
    val categoryId: Int?,
    val minPrice: Int?,
    val maxPrice: Int?,
    val specs: Map<String, String>,
    val sort: String,
    val page: Int,
    val limit: Int,
)

interface SearchRepository {
    fun search(query: SearchQuery): SearchResultRecord

    fun autocomplete(query: String): Map<String, List<Map<String, Any?>>>

    fun popularKeywords(limit: Int): List<Map<String, Any?>>

    fun saveRecentKeyword(
        userId: Int,
        keyword: String,
    ): SearchRecentKeywordRecord

    fun listRecentKeywords(userId: Int): List<SearchRecentKeywordRecord>

    fun removeRecentKeyword(
        userId: Int,
        recentId: Int,
    )

    fun clearRecentKeywords(userId: Int)

    fun getPreference(userId: Int): Boolean

    fun updatePreference(
        userId: Int,
        saveRecentSearches: Boolean,
    ): Boolean

    fun getWeights(): SearchWeightConfig

    fun updateWeights(
        nameWeight: Double,
        keywordWeight: Double,
        clickWeight: Double,
    ): SearchWeightConfig

    fun getIndexStatus(): SearchIndexStatusRecord

    fun reindexAllProducts(): Pair<String, Boolean>

    fun reindexProduct(productId: Int): Pair<String, Int>

    fun getOutboxSummary(): SearchOutboxSummaryRecord

    fun requeueFailed(limit: Int): Int
}
