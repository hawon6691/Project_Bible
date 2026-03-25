package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address.AddressRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart.CartRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price.PriceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product.ProductRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller.SellerRepository
import io.ktor.http.HttpStatusCode

class OrderService(
    private val repository: OrderRepository,
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val sellerRepository: SellerRepository,
    private val priceRepository: PriceRepository,
) {
    fun create(
        userId: Int,
        request: OrderCreateRequest,
    ): StubResponse {
        if (request.usePoint < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "usePoint는 0 이상이어야 합니다.")
        }
        val shippingAddress =
            addressRepository.findAddressById(userId, request.addressId)
                ?.let {
                    OrderShippingAddress(
                        recipientName = it.recipientName,
                        recipientPhone = it.phone,
                        zipCode = it.zipCode,
                        address = it.address,
                        addressDetail = it.addressDetail,
                    )
                }
                ?: throw PbShopException(HttpStatusCode.NotFound, "ADDRESS_NOT_FOUND", "배송지를 찾을 수 없습니다.")

        val orderItems = resolveItems(userId, request)
        val created =
            repository.createOrder(
                userId = userId,
                shippingAddress = shippingAddress,
                items = orderItems,
                pointUsed = request.usePoint,
                memo = request.memo?.trim()?.takeIf { it.isNotBlank() },
            )

        if (request.fromCart) {
            val targets = request.cartItemIds.toSet()
            if (targets.isEmpty()) {
                cartRepository.clearCart(userId)
            } else {
                targets.forEach { cartRepository.deleteCartItem(userId, it) }
            }
        }

        return StubResponse(status = HttpStatusCode.Created, data = detailPayload(created))
    }

    fun list(
        userId: Int,
        page: Int,
        limit: Int,
        status: String?,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val parsedStatus = status?.let(::parseStatus)
        val result = repository.listOrders(userId, queryPage, queryLimit, parsedStatus)
        return StubResponse(
            data = result.items.map(::summaryPayload),
            meta =
                mapOf(
                    "page" to queryPage,
                    "limit" to queryLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + queryLimit - 1) / queryLimit),
                ),
        )
    }

    fun detail(
        userId: Int,
        orderId: Int,
    ): StubResponse =
        StubResponse(
            data =
                detailPayload(
                    repository.findOrderDetailById(userId, orderId)
                        ?: throw PbShopException(HttpStatusCode.NotFound, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
                ),
        )

    fun cancel(
        userId: Int,
        orderId: Int,
    ): StubResponse {
        val order =
            repository.findOrderDetailById(userId, orderId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다.")
        if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.RETURNED) {
            throw PbShopException(HttpStatusCode.BadRequest, "ORDER_CANNOT_CANCEL", "현재 상태에서는 주문을 취소할 수 없습니다.")
        }
        return StubResponse(data = detailPayload(repository.cancelOrder(userId, orderId)))
    }

    fun adminList(
        page: Int,
        limit: Int,
        status: String?,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val parsedStatus = status?.let(::parseStatus)
        val result = repository.listAdminOrders(queryPage, queryLimit, parsedStatus)
        return StubResponse(
            data = result.items.map(::summaryPayload),
            meta =
                mapOf(
                    "page" to queryPage,
                    "limit" to queryLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + queryLimit - 1) / queryLimit),
                ),
        )
    }

    fun adminUpdateStatus(
        orderId: Int,
        request: OrderStatusUpdateRequest,
    ): StubResponse = StubResponse(data = detailPayload(repository.updateOrderStatus(orderId, parseStatus(request.status))))

    private fun resolveItems(
        userId: Int,
        request: OrderCreateRequest,
    ): List<NewOrderItem> {
        val resolved =
            if (request.fromCart) {
                val cartItems = cartRepository.listCartItems(userId)
                val selected =
                    if (request.cartItemIds.isEmpty()) {
                        cartItems
                    } else {
                        cartItems.filter { it.id in request.cartItemIds.toSet() }
                    }
                selected.map {
                    NewOrderItem(
                        productId = it.productId,
                        sellerId = it.sellerId,
                        productName = it.productName,
                        sellerName = it.sellerName,
                        selectedOptions = it.selectedOptions,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        totalPrice = it.totalPrice,
                    )
                }
            } else {
                request.items.map { item ->
                    if (item.quantity < 1) {
                        throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "quantity는 1 이상이어야 합니다.")
                    }
                    val product =
                        productRepository.findProductById(item.productId)
                            ?: throw PbShopException(HttpStatusCode.BadRequest, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
                    val seller =
                        sellerRepository.findSellerById(item.sellerId)
                            ?: throw PbShopException(HttpStatusCode.BadRequest, "SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.")
                    val priceEntry =
                        priceRepository.listProductPrices(item.productId)
                            .firstOrNull { it.sellerId == item.sellerId && it.isAvailable }
                    val unitPrice = priceEntry?.price ?: (product.lowestPrice ?: product.price)
                    NewOrderItem(
                        productId = item.productId,
                        sellerId = item.sellerId,
                        productName = product.name,
                        sellerName = seller.name,
                        selectedOptions = item.selectedOptions?.trim()?.takeIf { it.isNotBlank() },
                        quantity = item.quantity,
                        unitPrice = unitPrice,
                        totalPrice = unitPrice * item.quantity,
                    )
                }
            }

        if (resolved.isEmpty()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "주문 상품이 필요합니다.")
        }
        return resolved
    }

    private fun parseStatus(value: String): OrderStatus =
        runCatching { OrderStatus.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 주문 상태입니다.")
            }

    private fun summaryPayload(record: OrderSummaryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "orderNumber" to record.orderNumber,
            "status" to record.status.name,
            "totalAmount" to record.totalAmount,
            "pointUsed" to record.pointUsed,
            "finalAmount" to record.finalAmount,
            "itemCount" to record.itemCount,
            "createdAt" to record.createdAt.toString(),
        )

    private fun detailPayload(record: OrderDetailRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "orderNumber" to record.orderNumber,
            "status" to record.status.name,
            "items" to
                record.items.map {
                    mapOf(
                        "id" to it.id,
                        "productId" to it.productId,
                        "sellerId" to it.sellerId,
                        "productName" to it.productName,
                        "sellerName" to it.sellerName,
                        "selectedOptions" to it.selectedOptions,
                        "quantity" to it.quantity,
                        "unitPrice" to it.unitPrice,
                        "totalPrice" to it.totalPrice,
                        "isReviewed" to it.isReviewed,
                    )
                },
            "totalAmount" to record.totalAmount,
            "pointUsed" to record.pointUsed,
            "finalAmount" to record.finalAmount,
            "shippingAddress" to
                mapOf(
                    "recipientName" to record.shippingAddress.recipientName,
                    "recipientPhone" to record.shippingAddress.recipientPhone,
                    "zipCode" to record.shippingAddress.zipCode,
                    "address" to record.shippingAddress.address,
                    "addressDetail" to record.shippingAddress.addressDetail,
                ),
            "memo" to record.memo,
            "createdAt" to record.createdAt.toString(),
        )
}
