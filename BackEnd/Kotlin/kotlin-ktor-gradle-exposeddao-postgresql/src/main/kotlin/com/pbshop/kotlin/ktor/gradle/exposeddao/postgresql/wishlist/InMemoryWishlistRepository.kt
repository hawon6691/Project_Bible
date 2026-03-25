package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import java.time.Instant

class InMemoryWishlistRepository(
    seededItems: List<WishlistItemRecord> = emptyList(),
    private val productCatalog: Map<Int, Pair<String, Int>> = emptyMap(),
) : WishlistRepository {
    private val items = linkedMapOf<Int, WishlistItemRecord>()
    private var nextId = 1

    init {
        seededItems.forEach {
            items[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun productExists(productId: Int): Boolean = productCatalog.containsKey(productId)

    override fun listWishlist(
        userId: Int,
        page: Int,
        limit: Int,
    ): WishlistListResult {
        val filtered = items.values.filter { it.userId == userId }.sortedByDescending { it.id }
        val offset = (page - 1) * limit
        return WishlistListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findWishlistItem(
        userId: Int,
        productId: Int,
    ): WishlistItemRecord? = items.values.firstOrNull { it.userId == userId && it.productId == productId }

    override fun createWishlistItem(
        userId: Int,
        productId: Int,
    ): WishlistItemRecord {
        val catalog = requireNotNull(productCatalog[productId]) { "Product $productId not found" }
        val created =
            WishlistItemRecord(
                id = nextId++,
                userId = userId,
                productId = productId,
                productName = catalog.first,
                thumbnailUrl = "https://img.example.com/products/$productId.jpg",
                lowestPrice = catalog.second,
                wishlistedAt = Instant.now(),
            )
        items[created.id] = created
        return created
    }

    override fun deleteWishlistItem(
        userId: Int,
        productId: Int,
    ) {
        items.values.firstOrNull { it.userId == userId && it.productId == productId }?.let { items.remove(it.id) }
    }

    companion object {
        fun seeded(): InMemoryWishlistRepository =
            InMemoryWishlistRepository(
                seededItems =
                    listOf(
                        WishlistItemRecord(1, 4, 1, "게이밍 노트북 A15", "https://img.example.com/p1-thumb.jpg", 1720000, Instant.now().minusSeconds(3600)),
                        WishlistItemRecord(2, 4, 2, "사무용 노트북 Slim", "https://img.example.com/p2-thumb.jpg", 940000, Instant.now().minusSeconds(1800)),
                        WishlistItemRecord(3, 5, 3, "미니 데스크탑 Pro", "https://img.example.com/p3-thumb.jpg", 1360000, Instant.now().minusSeconds(900)),
                    ),
                productCatalog =
                    mapOf(
                        1 to ("게이밍 노트북 A15" to 1720000),
                        2 to ("사무용 노트북 Slim" to 940000),
                        3 to ("미니 데스크탑 Pro" to 1360000),
                    ),
            )
    }
}
