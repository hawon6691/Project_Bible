package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import java.time.Instant

class InMemoryCartRepository(
    seededItems: List<CartItemRecord> = emptyList(),
    private val productIds: Set<Int> = emptySet(),
    private val sellerIds: Set<Int> = emptySet(),
) : CartRepository {
    private val items = linkedMapOf<Int, CartItemRecord>()
    private var nextId = 1

    init {
        seededItems.forEach {
            items[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun productExists(productId: Int): Boolean = productIds.contains(productId)

    override fun sellerExists(sellerId: Int): Boolean = sellerIds.contains(sellerId)

    override fun listCartItems(userId: Int): List<CartItemRecord> =
        items.values.filter { it.userId == userId }.sortedBy { it.id }

    override fun findCartItemById(
        userId: Int,
        itemId: Int,
    ): CartItemRecord? = items[itemId]?.takeIf { it.userId == userId }

    override fun addCartItem(
        userId: Int,
        newItem: NewCartItem,
    ): CartItemRecord {
        val existing =
            items.values.firstOrNull {
                it.userId == userId &&
                    it.productId == newItem.productId &&
                    it.sellerId == newItem.sellerId &&
                    it.selectedOptions == newItem.selectedOptions
            }
        if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + newItem.quantity, totalPrice = existing.unitPrice * (existing.quantity + newItem.quantity))
            items[existing.id] = updated
            return updated
        }

        val unitPrice =
            when (newItem.productId) {
                1 -> 1720000
                2 -> 940000
                3 -> 1360000
                else -> 990000
            }
        val created =
            CartItemRecord(
                id = nextId++,
                userId = userId,
                productId = newItem.productId,
                sellerId = newItem.sellerId,
                productName =
                    when (newItem.productId) {
                        1 -> "게이밍 노트북 A15"
                        2 -> "사무용 노트북 Slim"
                        3 -> "미니 데스크탑 Pro"
                        else -> "새 상품"
                    },
                sellerName =
                    when (newItem.sellerId) {
                        1 -> "공식몰"
                        2 -> "테크마켓"
                        3 -> "딜마트"
                        else -> "신규 판매처"
                    },
                thumbnailUrl = "https://img.example.com/products/${newItem.productId}.jpg",
                selectedOptions = newItem.selectedOptions,
                quantity = newItem.quantity,
                unitPrice = unitPrice,
                totalPrice = unitPrice * newItem.quantity,
                createdAt = Instant.now(),
            )
        items[created.id] = created
        return created
    }

    override fun updateCartItemQuantity(
        userId: Int,
        itemId: Int,
        quantity: Int,
    ): CartItemRecord {
        val current = requireNotNull(findCartItemById(userId, itemId)) { "Cart item $itemId not found" }
        val updated = current.copy(quantity = quantity, totalPrice = current.unitPrice * quantity)
        items[itemId] = updated
        return updated
    }

    override fun deleteCartItem(
        userId: Int,
        itemId: Int,
    ) {
        findCartItemById(userId, itemId)?.also { items.remove(it.id) } ?: error("Cart item $itemId not found")
    }

    override fun clearCart(userId: Int) {
        items.values.removeIf { it.userId == userId }
    }

    companion object {
        fun seeded(): InMemoryCartRepository =
            InMemoryCartRepository(
                seededItems =
                    listOf(
                        CartItemRecord(1, 4, 1, 1, "게이밍 노트북 A15", "공식몰", "https://img.example.com/p1-thumb.jpg", "RAM:16GB,SSD:1TB", 1, 1720000, 1720000, Instant.now().minusSeconds(3600)),
                        CartItemRecord(2, 4, 2, 2, "사무용 노트북 Slim", "테크마켓", "https://img.example.com/p2-thumb.jpg", "색상:실버", 1, 940000, 940000, Instant.now().minusSeconds(1800)),
                        CartItemRecord(3, 5, 3, 1, "미니 데스크탑 Pro", "공식몰", "https://img.example.com/p3-thumb.jpg", null, 1, 1360000, 1360000, Instant.now().minusSeconds(600)),
                    ),
                productIds = setOf(1, 2, 3),
                sellerIds = setOf(1, 2, 3),
            )
    }
}
