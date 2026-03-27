package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductSpecsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SearchHistoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SearchLogsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SearchSynonymsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SpecDefinitionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

class ExposedDaoSearchRepository(
    private val databaseFactory: DatabaseFactory,
) : SearchRepository {
    private val preferenceByUser = ConcurrentHashMap<Int, Boolean>()
    private var weights = SearchWeightConfig(1.0, 1.5, 0.8)
    private var lastIndexedAt: Instant? = null

    override fun search(query: SearchQuery): SearchResultRecord =
        databaseFactory.withTransaction {
            val normalized = query.q.lowercase()
            val rows =
                ProductsTable.innerJoin(CategoriesTable)
                    .selectAll()
                    .where { ProductsTable.deletedAt.isNull() }
                    .orderBy(ProductsTable.createdAt to SortOrder.DESC)
                    .map(::toProductRow)
                    .filter { row ->
                        matchesKeyword(row, normalized) &&
                            (query.categoryId == null || row.categoryId == query.categoryId) &&
                            (query.minPrice == null || row.lowestPrice >= query.minPrice) &&
                            (query.maxPrice == null || row.lowestPrice <= query.maxPrice) &&
                            matchesSpecs(row, query.specs)
                    }.map { row -> row to calculateScore(row, normalized) }

            val sorted =
                when (query.sort) {
                    "price_asc" -> rows.sortedBy { it.first.lowestPrice }
                    "price_desc" -> rows.sortedByDescending { it.first.lowestPrice }
                    "newest" -> rows.sortedByDescending { it.first.createdAt }
                    "rating_desc" -> rows.sortedByDescending { it.first.averageRating }
                    "rating_asc" -> rows.sortedBy { it.first.averageRating }
                    else -> rows.sortedByDescending { it.second }
                }
            val totalCount = sorted.size
            val offset = (query.page - 1) * query.limit
            val pageItems = sorted.drop(offset).take(query.limit)
            logSearch(query, totalCount)

            SearchResultRecord(
                hits =
                    pageItems.map { (row, score) ->
                        SearchHitRecord(
                            id = row.id,
                            name = row.name,
                            thumbnailUrl = row.thumbnailUrl,
                            lowestPrice = row.lowestPrice,
                            categoryName = row.categoryName,
                            score = score,
                        )
                    },
                categories =
                    sorted.groupBy { it.first.categoryId to (it.first.categoryName ?: "") }
                        .map { (key, items) -> SearchFacetCategory(key.first, key.second, items.size) }
                        .sortedByDescending { it.count },
                suggestions = suggestionsFor(normalized),
                totalCount = totalCount,
            )
        }

    override fun autocomplete(query: String): Map<String, List<Map<String, Any?>>> =
        databaseFactory.withTransaction {
            val normalized = query.lowercase()
            val products =
                ProductsTable.selectAll()
                    .where { ProductsTable.deletedAt.isNull() }
                    .map {
                        mapOf(
                            "id" to it[ProductsTable.id].value,
                            "name" to it[ProductsTable.name],
                            "thumbnailUrl" to it[ProductsTable.thumbnailUrl],
                            "lowestPrice" to (it[ProductsTable.lowestPrice] ?: it[ProductsTable.price]),
                        )
                    }.filter { (it["name"] as String).contains(normalized, true) }
                    .take(5)
            val categories =
                CategoriesTable.selectAll()
                    .map { mapOf("id" to it[CategoriesTable.id].value, "name" to it[CategoriesTable.name]) }
                    .filter { (it["name"] as String).contains(normalized, true) }
                    .take(5)
            mapOf(
                "keywords" to suggestionsFor(normalized).take(5).map { mapOf("value" to it) },
                "products" to products,
                "categories" to categories,
            )
        }

    override fun popularKeywords(limit: Int): List<Map<String, Any?>> =
        databaseFactory.withTransaction {
            SearchLogsTable.selectAll()
                .where { SearchLogsTable.searchedAt greaterEq Instant.now().minus(30, ChronoUnit.DAYS) }
                .groupBy { it[SearchLogsTable.keyword] }
                .map { (keyword, rows) -> keyword to rows.size }
                .sortedByDescending { it.second }
                .take(limit)
                .mapIndexed { index, (keyword, count) ->
                    mapOf("rank" to index + 1, "keyword" to keyword, "searchCount" to count, "rankChange" to 0)
                }
        }

    override fun saveRecentKeyword(
        userId: Int,
        keyword: String,
    ): SearchRecentKeywordRecord =
        databaseFactory.withTransaction {
            SearchHistoriesTable.deleteWhere { (SearchHistoriesTable.user eq userId) and (SearchHistoriesTable.keyword eq keyword) }
            val now = Instant.now()
            val insertedId =
                SearchHistoriesTable.insertAndGetId { row ->
                    row[user] = EntityID(userId, UsersTable)
                    row[SearchHistoriesTable.keyword] = keyword
                    row[createdAt] = now
                    row[updatedAt] = now
                }.value
            SearchRecentKeywordRecord(insertedId, keyword, now)
        }

    override fun listRecentKeywords(userId: Int): List<SearchRecentKeywordRecord> =
        databaseFactory.withTransaction {
            SearchHistoriesTable.selectAll()
                .where { SearchHistoriesTable.user eq userId }
                .orderBy(SearchHistoriesTable.createdAt to SortOrder.DESC)
                .map {
                    SearchRecentKeywordRecord(
                        id = it[SearchHistoriesTable.id].value,
                        keyword = it[SearchHistoriesTable.keyword],
                        createdAt = it[SearchHistoriesTable.createdAt],
                    )
                }
        }

    override fun removeRecentKeyword(
        userId: Int,
        recentId: Int,
    ) {
        databaseFactory.withTransaction {
            SearchHistoriesTable.deleteWhere { (SearchHistoriesTable.id eq recentId) and (SearchHistoriesTable.user eq userId) }
        }
    }

    override fun clearRecentKeywords(userId: Int) {
        databaseFactory.withTransaction {
            SearchHistoriesTable.deleteWhere { SearchHistoriesTable.user eq userId }
        }
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

    override fun getIndexStatus(): SearchIndexStatusRecord =
        databaseFactory.withTransaction {
            SearchIndexStatusRecord(
                status = "HEALTHY",
                documents = ProductsTable.selectAll().where { ProductsTable.deletedAt.isNull() }.count().toInt(),
                lastIndexedAt = lastIndexedAt ?: SearchLogsTable.slice(SearchLogsTable.searchedAt.max()).selectAll().singleOrNull()?.get(SearchLogsTable.searchedAt.max()),
            )
        }

    override fun reindexAllProducts(): Pair<String, Boolean> {
        lastIndexedAt = Instant.now()
        return "Reindex queued." to true
    }

    override fun reindexProduct(productId: Int): Pair<String, Int> {
        lastIndexedAt = Instant.now()
        return "Product reindex queued." to productId
    }

    private fun matchesKeyword(
        row: SearchProductRow,
        normalized: String,
    ): Boolean =
        row.name.contains(normalized, true) ||
            row.description.contains(normalized, true) ||
            (row.categoryName?.contains(normalized, true) == true) ||
            row.specs.any { it.first.contains(normalized, true) || it.second.contains(normalized, true) }

    private fun matchesSpecs(
        row: SearchProductRow,
        specs: Map<String, String>,
    ): Boolean =
        specs.all { (name, expected) ->
            row.specs.any { it.first.equals(name, true) && it.second.contains(expected, true) }
        }

    private fun calculateScore(
        row: SearchProductRow,
        normalizedQuery: String,
    ): Double {
        var score = row.popularityScore
        if (row.name.contains(normalizedQuery, true)) score += weights.nameWeight * 5.0
        if (row.description.contains(normalizedQuery, true)) score += weights.keywordWeight * 2.0
        if (row.specs.any { it.second.contains(normalizedQuery, true) }) score += weights.keywordWeight
        score += weights.clickWeight * row.averageRating
        return score
    }

    private fun suggestionsFor(normalized: String): List<String> =
        databaseFactory.withTransaction {
            val fromLogs =
                SearchLogsTable.selectAll()
                    .map { it[SearchLogsTable.keyword] }
                    .filter { it.contains(normalized, true) }
            val fromSynonyms =
                SearchSynonymsTable.selectAll()
                    .where { SearchSynonymsTable.isActive eq true }
                    .flatMap {
                        buildList {
                            add(it[SearchSynonymsTable.word])
                            addAll(parseJsonArray(it[SearchSynonymsTable.synonyms]))
                        }
                    }.filter { it.contains(normalized, true) }
            (fromLogs + fromSynonyms).distinct().take(10)
        }

    private fun logSearch(
        query: SearchQuery,
        totalCount: Int,
    ) {
        SearchLogsTable.insert { row ->
            row[user] = null
            row[keyword] = query.q
            row[resultCount] = totalCount
                row[categoryId] = query.categoryId
                row[filters] = query.specs.entries.joinToString(prefix = "{", postfix = "}") { "\"${it.key}\":\"${it.value}\"" }
                row[responseTimeMs] = 30
                row[searchedAt] = Instant.now()
            }
    }

    private fun toProductRow(row: ResultRow): SearchProductRow {
        val productId = row[ProductsTable.id].value
        val specs =
            ProductSpecsTable.innerJoin(SpecDefinitionsTable)
                .selectAll()
                .where { ProductSpecsTable.product eq productId }
                .map { it[SpecDefinitionsTable.name] to it[ProductSpecsTable.value] }
        val marketPrice =
            PriceEntriesTable.selectAll()
                .where { (PriceEntriesTable.product eq productId) and (PriceEntriesTable.isAvailable eq true) }
                .map { it[PriceEntriesTable.totalPrice] }
                .minOrNull()
        return SearchProductRow(
            id = productId,
            name = row[ProductsTable.name],
            description = row[ProductsTable.description],
            thumbnailUrl = row[ProductsTable.thumbnailUrl],
            lowestPrice = marketPrice ?: row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
            categoryId = row[ProductsTable.category].value,
            categoryName = row[CategoriesTable.name],
            createdAt = row[ProductsTable.createdAt],
            averageRating = row[ProductsTable.averageRating].toDouble(),
            popularityScore = row[ProductsTable.popularityScore].toDouble(),
            specs = specs,
        )
    }

    private fun parseJsonArray(raw: String): List<String> =
        runCatching {
            Json.parseToJsonElement(raw).jsonArray.map { it.jsonPrimitive.content }
        }.getOrDefault(emptyList())

    private data class SearchProductRow(
        val id: Int,
        val name: String,
        val description: String,
        val thumbnailUrl: String?,
        val lowestPrice: Int,
        val categoryId: Int,
        val categoryName: String?,
        val createdAt: Instant,
        val averageRating: Double,
        val popularityScore: Double,
        val specs: List<Pair<String, String>>,
    )
}
