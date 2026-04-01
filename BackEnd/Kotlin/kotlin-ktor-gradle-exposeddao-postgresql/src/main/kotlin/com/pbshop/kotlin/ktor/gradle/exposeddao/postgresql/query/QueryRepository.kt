package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import java.time.Instant

data class ProductQueryViewRecord(
    val productId: Int,
    val categoryId: Int,
    val name: String,
    val thumbnailUrl: String?,
    val status: String,
    val basePrice: Int,
    val lowestPrice: Int?,
    val sellerCount: Int,
    val averageRating: Double,
    val reviewCount: Int,
    val viewCount: Int,
    val popularityScore: Double,
    val syncedAt: Instant,
    val updatedAt: Instant,
)

enum class ProductQuerySort(
    val apiValue: String,
) {
    NEWEST("newest"),
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc"),
    POPULARITY("popularity"),
    RATING("rating"),
    ;

    companion object {
        fun fromQuery(value: String?): ProductQuerySort =
            entries.firstOrNull { it.apiValue == value?.trim()?.lowercase() } ?: NEWEST
    }
}

data class ProductQueryViewQuery(
    val categoryId: Int? = null,
    val keyword: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val sort: ProductQuerySort = ProductQuerySort.NEWEST,
    val page: Int = 1,
    val limit: Int = 20,
)

data class ProductQueryViewListResult(
    val items: List<ProductQueryViewRecord>,
    val totalCount: Int,
)

interface QueryRepository {
    fun listProducts(query: ProductQueryViewQuery): ProductQueryViewListResult

    fun findProductDetail(productId: Int): ProductQueryViewRecord?

    fun syncProduct(productId: Int): ProductQueryViewRecord?

    fun rebuildAll(): Int
}
