package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class SearchService(
    private val repository: SearchRepository,
) {
    fun search(
        q: String?,
        categoryId: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        specs: String?,
        sort: String?,
        page: Int?,
        limit: Int?,
    ): StubResponse {
        val keyword = q?.trim().orEmpty()
        if (keyword.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "q는 필수입니다.")
        }
        val query =
            SearchQuery(
                q = keyword,
                categoryId = categoryId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                specs = parseSpecs(specs),
                sort = sort?.trim()?.lowercase() ?: "relevance",
                page = (page ?: 1).coerceAtLeast(1),
                limit = (limit ?: 20).coerceIn(1, 100),
            )
        val result = repository.search(query)
        return StubResponse(
            data =
                mapOf(
                    "hits" to
                        result.hits.map {
                            mapOf(
                                "id" to it.id,
                                "name" to it.name,
                                "thumbnailUrl" to it.thumbnailUrl,
                                "lowestPrice" to it.lowestPrice,
                                "categoryName" to it.categoryName,
                                "score" to it.score,
                            )
                        },
                    "facets" to mapOf("categories" to result.categories.map { mapOf("id" to it.id, "name" to it.name, "count" to it.count) }),
                    "suggestions" to result.suggestions,
                    "totalCount" to result.totalCount,
                ),
                meta =
                    mapOf(
                        "page" to query.page,
                        "limit" to query.limit,
                        "totalCount" to result.totalCount,
                        "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + query.limit - 1) / query.limit),
                    ),
            )
    }

    fun autocomplete(q: String?): StubResponse {
        val keyword = q?.trim().orEmpty()
        if (keyword.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "q는 필수입니다.")
        }
        return StubResponse(data = repository.autocomplete(keyword))
    }

    fun popular(limit: Int?): StubResponse =
        StubResponse(data = repository.popularKeywords((limit ?: 10).coerceIn(1, 100)))

    fun saveRecent(
        userId: Int,
        request: SaveRecentSearchRequest,
    ): StubResponse {
        if (request.keyword.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "keyword는 비어 있을 수 없습니다.")
        }
        if (!repository.getPreference(userId)) {
            return StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 0, "keyword" to request.keyword.trim(), "createdAt" to ""))
        }
        val created = repository.saveRecentKeyword(userId, request.keyword.trim())
        return StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to created.id, "keyword" to created.keyword, "createdAt" to created.createdAt.toString()))
    }

    fun recent(userId: Int): StubResponse =
        StubResponse(data = repository.listRecentKeywords(userId).map { mapOf("id" to it.id, "keyword" to it.keyword, "createdAt" to it.createdAt.toString()) })

    fun deleteRecent(
        userId: Int,
        id: Int,
    ): StubResponse {
        repository.removeRecentKeyword(userId, id)
        return StubResponse(data = mapOf("message" to "Recent search deleted."))
    }

    fun clearRecent(userId: Int): StubResponse {
        repository.clearRecentKeywords(userId)
        return StubResponse(data = mapOf("message" to "Recent search history cleared."))
    }

    fun updatePreference(
        userId: Int,
        request: SearchPreferenceRequest,
    ): StubResponse = StubResponse(data = mapOf("saveRecentSearches" to repository.updatePreference(userId, request.saveRecentSearches)))

    fun getWeights(): StubResponse = with(repository.getWeights()) { StubResponse(data = mapOf("nameWeight" to nameWeight, "keywordWeight" to keywordWeight, "clickWeight" to clickWeight)) }

    fun updateWeights(request: SearchWeightRequest): StubResponse =
        repository.updateWeights(request.nameWeight, request.keywordWeight, request.clickWeight).let {
            StubResponse(data = mapOf("nameWeight" to it.nameWeight, "keywordWeight" to it.keywordWeight, "clickWeight" to it.clickWeight))
        }

    fun indexStatus(): StubResponse =
        repository.getIndexStatus().let {
            StubResponse(
                data =
                    mapOf(
                        "status" to it.status,
                        "documents" to it.documents,
                        "lastIndexedAt" to it.lastIndexedAt?.toString(),
                    ),
            )
        }

    fun reindexAll(): StubResponse =
        repository.reindexAllProducts().let { StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to it.first, "queued" to it.second)) }

    fun reindexProduct(productId: Int): StubResponse =
        repository.reindexProduct(productId).let { StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to it.first, "productId" to it.second)) }

    private fun parseSpecs(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val parsed = Json.parseToJsonElement(raw)
            if (parsed !is JsonObject) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "specs는 JSON object 형식이어야 합니다.")
            }
            parsed.entries.associate { (key, value) -> key to value.jsonPrimitive.content }
        }.getOrElse {
            if (it is PbShopException) throw it
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "specs 형식이 올바르지 않습니다.")
        }
    }
}
