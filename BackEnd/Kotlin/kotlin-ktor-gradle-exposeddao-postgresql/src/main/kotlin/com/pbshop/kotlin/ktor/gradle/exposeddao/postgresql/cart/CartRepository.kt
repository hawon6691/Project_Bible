package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import java.time.Instant

data class CartItemRecord(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val sellerId: Int,
    val productName: String,
    val sellerName: String,
    val thumbnailUrl: String?,
    val selectedOptions: String?,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val createdAt: Instant,
)

data class NewCartItem(
    val productId: Int,
    val sellerId: Int,
    val selectedOptions: String?,
    val quantity: Int,
)

interface CartRepository {
    fun productExists(productId: Int): Boolean

    fun sellerExists(sellerId: Int): Boolean

    fun listCartItems(userId: Int): List<CartItemRecord>

    fun findCartItemById(
        userId: Int,
        itemId: Int,
    ): CartItemRecord?

    fun addCartItem(
        userId: Int,
        newItem: NewCartItem,
    ): CartItemRecord

    fun updateCartItemQuantity(
        userId: Int,
        itemId: Int,
        quantity: Int,
    ): CartItemRecord

    fun deleteCartItem(
        userId: Int,
        itemId: Int,
    )

    fun clearCart(userId: Int)
}
