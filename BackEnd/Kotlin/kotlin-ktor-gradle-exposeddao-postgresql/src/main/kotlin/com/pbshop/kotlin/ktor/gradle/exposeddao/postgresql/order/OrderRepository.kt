package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderStatus
import java.time.Instant

data class OrderShippingAddress(
    val recipientName: String,
    val recipientPhone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String?,
)

data class OrderItemRecord(
    val id: Int,
    val productId: Int,
    val sellerId: Int,
    val productName: String,
    val sellerName: String,
    val selectedOptions: String?,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val isReviewed: Boolean,
)

data class NewOrderItem(
    val productId: Int,
    val sellerId: Int,
    val productName: String,
    val sellerName: String,
    val selectedOptions: String?,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
)

data class OrderSummaryRecord(
    val id: Int,
    val userId: Int,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Int,
    val pointUsed: Int,
    val finalAmount: Int,
    val itemCount: Int,
    val createdAt: Instant,
)

data class OrderDetailRecord(
    val id: Int,
    val userId: Int,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Int,
    val pointUsed: Int,
    val finalAmount: Int,
    val shippingAddress: OrderShippingAddress,
    val memo: String?,
    val items: List<OrderItemRecord>,
    val createdAt: Instant,
)

data class OrderListResult(
    val items: List<OrderSummaryRecord>,
    val totalCount: Int,
)

interface OrderRepository {
    fun createOrder(
        userId: Int,
        shippingAddress: OrderShippingAddress,
        items: List<NewOrderItem>,
        pointUsed: Int,
        memo: String?,
    ): OrderDetailRecord

    fun listOrders(
        userId: Int,
        page: Int,
        limit: Int,
        status: OrderStatus? = null,
    ): OrderListResult

    fun findOrderDetailById(
        userId: Int,
        orderId: Int,
    ): OrderDetailRecord?

    fun cancelOrder(
        userId: Int,
        orderId: Int,
    ): OrderDetailRecord

    fun listAdminOrders(
        page: Int,
        limit: Int,
        status: OrderStatus? = null,
    ): OrderListResult

    fun updateOrderStatus(
        orderId: Int,
        status: OrderStatus,
    ): OrderDetailRecord
}
