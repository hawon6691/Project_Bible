package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import java.time.Instant

data class WishlistItemRecord(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
    val wishlistedAt: Instant,
)

data class WishlistListResult(
    val items: List<WishlistItemRecord>,
    val totalCount: Int,
)

interface WishlistRepository {
    fun productExists(productId: Int): Boolean

    fun listWishlist(
        userId: Int,
        page: Int,
        limit: Int,
    ): WishlistListResult

    fun findWishlistItem(
        userId: Int,
        productId: Int,
    ): WishlistItemRecord?

    fun createWishlistItem(
        userId: Int,
        productId: Int,
    ): WishlistItemRecord

    fun deleteWishlistItem(
        userId: Int,
        productId: Int,
    )
}
