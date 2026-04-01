package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import java.time.Instant

class InMemoryQueryRepository private constructor(
    private val items: MutableMap<Int, ProductQueryViewRecord>,
) : QueryRepository {
    override fun listProducts(query: ProductQueryViewQuery): ProductQueryViewListResult {
        val filtered =
            items.values
                .asSequence()
                .filter { query.categoryId == null || it.categoryId == query.categoryId }
                .filter { query.keyword.isNullOrBlank() || it.name.contains(query.keyword.trim(), ignoreCase = true) }
                .filter { query.minPrice == null || (it.lowestPrice ?: it.basePrice) >= query.minPrice }
                .filter { query.maxPrice == null || (it.lowestPrice ?: it.basePrice) <= query.maxPrice }
                .sortedWith(sortComparator(query.sort))
                .toList()
        val page = query.page.coerceAtLeast(1)
        val limit = query.limit.coerceIn(1, 100)
        val start = (page - 1) * limit
        val paged = if (start >= filtered.size) emptyList() else filtered.drop(start).take(limit)
        return ProductQueryViewListResult(items = paged, totalCount = filtered.size)
    }

    override fun findProductDetail(productId: Int): ProductQueryViewRecord? = items[productId]

    override fun syncProduct(productId: Int): ProductQueryViewRecord? {
        val current = items[productId] ?: return null
        val synced = current.copy(syncedAt = Instant.now(), updatedAt = Instant.now())
        items[productId] = synced
        return synced
    }

    override fun rebuildAll(): Int {
        val now = Instant.now()
        items.replaceAll { _, value -> value.copy(syncedAt = now, updatedAt = now) }
        return items.size
    }

    companion object {
        fun seeded(): InMemoryQueryRepository {
            val now = Instant.now()
            return InMemoryQueryRepository(
                mutableMapOf(
                    1 to
                        ProductQueryViewRecord(
                            productId = 1,
                            categoryId = 1,
                            name = "PB GalaxyBook 4 Pro",
                            thumbnailUrl = "/images/products/galaxybook4pro.webp",
                            status = "ON_SALE",
                            basePrice = 1590000,
                            lowestPrice = 1499000,
                            sellerCount = 2,
                            averageRating = 4.7,
                            reviewCount = 18,
                            viewCount = 240,
                            popularityScore = 85.4,
                            syncedAt = now,
                            updatedAt = now,
                        ),
                    2 to
                        ProductQueryViewRecord(
                            productId = 2,
                            categoryId = 1,
                            name = "PB Ultra Laptop 15",
                            thumbnailUrl = "/images/products/ultra15.webp",
                            status = "ON_SALE",
                            basePrice = 1290000,
                            lowestPrice = 1190000,
                            sellerCount = 1,
                            averageRating = 4.3,
                            reviewCount = 8,
                            viewCount = 120,
                            popularityScore = 61.2,
                            syncedAt = now,
                            updatedAt = now,
                        ),
                ),
            )
        }

        private fun sortComparator(sort: ProductQuerySort): Comparator<ProductQueryViewRecord> =
            when (sort) {
                ProductQuerySort.PRICE_ASC ->
                    compareBy<ProductQueryViewRecord> { it.lowestPrice ?: it.basePrice }.thenByDescending { it.updatedAt }
                ProductQuerySort.PRICE_DESC ->
                    compareByDescending<ProductQueryViewRecord> { it.lowestPrice ?: it.basePrice }.thenByDescending { it.updatedAt }
                ProductQuerySort.POPULARITY ->
                    compareByDescending<ProductQueryViewRecord> { it.popularityScore }.thenByDescending { it.viewCount }
                ProductQuerySort.RATING ->
                    compareByDescending<ProductQueryViewRecord> { it.averageRating }.thenByDescending { it.reviewCount }
                ProductQuerySort.NEWEST ->
                    compareByDescending<ProductQueryViewRecord> { it.updatedAt }
            }
    }
}
