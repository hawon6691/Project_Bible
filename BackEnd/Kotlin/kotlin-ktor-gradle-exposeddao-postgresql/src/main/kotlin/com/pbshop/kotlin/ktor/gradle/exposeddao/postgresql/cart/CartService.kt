package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class CartService(
    private val repository: CartRepository,
) {
    fun list(userId: Int): StubResponse =
        StubResponse(
            data = repository.listCartItems(userId).map(::payload),
        )

    fun add(
        userId: Int,
        request: CartCreateRequest,
    ): StubResponse {
        validateQuantity(request.quantity)
        ensureProductAndSeller(request.productId, request.sellerId)
        val created =
            repository.addCartItem(
                userId,
                NewCartItem(
                    productId = request.productId,
                    sellerId = request.sellerId,
                    selectedOptions = request.selectedOptions?.trim()?.takeIf { it.isNotBlank() },
                    quantity = request.quantity,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = payload(created))
    }

    fun updateQuantity(
        userId: Int,
        itemId: Int,
        request: CartUpdateRequest,
    ): StubResponse {
        validateQuantity(request.quantity)
        val updated = repository.updateCartItemQuantity(userId, itemId, request.quantity)
        return StubResponse(data = payload(updated))
    }

    fun delete(
        userId: Int,
        itemId: Int,
    ): StubResponse {
        requireItem(userId, itemId)
        repository.deleteCartItem(userId, itemId)
        return StubResponse(data = mapOf("message" to "Cart item removed."))
    }

    fun clear(userId: Int): StubResponse {
        repository.clearCart(userId)
        return StubResponse(data = mapOf("message" to "Cart cleared."))
    }

    private fun ensureProductAndSeller(
        productId: Int,
        sellerId: Int,
    ) {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        if (!repository.sellerExists(sellerId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.")
        }
    }

    private fun requireItem(
        userId: Int,
        itemId: Int,
    ): CartItemRecord =
        repository.findCartItemById(userId, itemId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "CART_ITEM_NOT_FOUND", "장바구니 항목을 찾을 수 없습니다.")

    private fun validateQuantity(quantity: Int) {
        if (quantity < 1) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "quantity는 1 이상이어야 합니다.")
        }
    }

    private fun payload(record: CartItemRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "productId" to record.productId,
            "sellerId" to record.sellerId,
            "productName" to record.productName,
            "sellerName" to record.sellerName,
            "thumbnailUrl" to record.thumbnailUrl,
            "selectedOptions" to record.selectedOptions,
            "quantity" to record.quantity,
            "price" to record.unitPrice,
            "totalPrice" to record.totalPrice,
            "createdAt" to record.createdAt.toString(),
        )
}
