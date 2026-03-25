package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderStatus
import java.time.Instant

class InMemoryOrderRepository(
    seededSummaries: List<OrderSummaryRecord> = emptyList(),
    seededDetails: List<OrderDetailRecord> = emptyList(),
) : OrderRepository {
    private val summaries = linkedMapOf<Int, OrderSummaryRecord>()
    private val details = linkedMapOf<Int, OrderDetailRecord>()
    private var nextOrderId = 1
    private var nextOrderItemId = 1

    init {
        seededSummaries.forEach {
            summaries[it.id] = it
            nextOrderId = maxOf(nextOrderId, it.id + 1)
        }
        seededDetails.forEach {
            details[it.id] = it
            nextOrderId = maxOf(nextOrderId, it.id + 1)
            nextOrderItemId = maxOf(nextOrderItemId, (it.items.maxOfOrNull { item -> item.id } ?: 0) + 1)
        }
    }

    override fun createOrder(
        userId: Int,
        shippingAddress: OrderShippingAddress,
        items: List<NewOrderItem>,
        pointUsed: Int,
        memo: String?,
    ): OrderDetailRecord {
        val now = Instant.now()
        val id = nextOrderId++
        val totalAmount = items.sumOf { it.totalPrice }
        val finalAmount = (totalAmount - pointUsed).coerceAtLeast(0)
        val detail =
            OrderDetailRecord(
                id = id,
                userId = userId,
                orderNumber = "ORD-${now.epochSecond}-$id",
                status = OrderStatus.ORDER_PLACED,
                totalAmount = totalAmount,
                pointUsed = pointUsed,
                finalAmount = finalAmount,
                shippingAddress = shippingAddress,
                memo = memo,
                items =
                    items.map {
                        OrderItemRecord(
                            id = nextOrderItemId++,
                            productId = it.productId,
                            sellerId = it.sellerId,
                            productName = it.productName,
                            sellerName = it.sellerName,
                            selectedOptions = it.selectedOptions,
                            quantity = it.quantity,
                            unitPrice = it.unitPrice,
                            totalPrice = it.totalPrice,
                            isReviewed = false,
                        )
                    },
                createdAt = now,
            )
        details[id] = detail
        summaries[id] =
            OrderSummaryRecord(
                id = id,
                userId = userId,
                orderNumber = detail.orderNumber,
                status = detail.status,
                totalAmount = detail.totalAmount,
                pointUsed = detail.pointUsed,
                finalAmount = detail.finalAmount,
                itemCount = detail.items.size,
                createdAt = now,
            )
        return detail
    }

    override fun listOrders(
        userId: Int,
        page: Int,
        limit: Int,
        status: OrderStatus?,
    ): OrderListResult {
        val filtered =
            summaries.values.filter { it.userId == userId && (status == null || it.status == status) }.sortedByDescending { it.id }
        val offset = (page - 1) * limit
        return OrderListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findOrderDetailById(
        userId: Int,
        orderId: Int,
    ): OrderDetailRecord? = details[orderId]?.takeIf { it.userId == userId }

    override fun cancelOrder(
        userId: Int,
        orderId: Int,
    ): OrderDetailRecord {
        val current = requireNotNull(findOrderDetailById(userId, orderId)) { "Order $orderId not found" }
        val updated = current.copy(status = OrderStatus.CANCELLED)
        details[orderId] = updated
        summaries[orderId] = requireNotNull(summaries[orderId]).copy(status = OrderStatus.CANCELLED)
        return updated
    }

    override fun listAdminOrders(
        page: Int,
        limit: Int,
        status: OrderStatus?,
    ): OrderListResult {
        val filtered = summaries.values.filter { status == null || it.status == status }.sortedByDescending { it.id }
        val offset = (page - 1) * limit
        return OrderListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun updateOrderStatus(
        orderId: Int,
        status: OrderStatus,
    ): OrderDetailRecord {
        val current = requireNotNull(details[orderId]) { "Order $orderId not found" }
        val updated = current.copy(status = status)
        details[orderId] = updated
        summaries[orderId] = requireNotNull(summaries[orderId]).copy(status = status)
        return updated
    }

    companion object {
        fun seeded(): InMemoryOrderRepository {
            val now = Instant.now()
            val detail1 =
                OrderDetailRecord(
                    id = 1,
                    userId = 4,
                    orderNumber = "ORD-20260225-A001",
                    status = OrderStatus.DELIVERED,
                    totalAmount = 2660000,
                    pointUsed = 20000,
                    finalAmount = 2640000,
                    shippingAddress = OrderShippingAddress("홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 123", "101동 1001호"),
                    memo = "문 앞에 놓아주세요",
                    items =
                        listOf(
                            OrderItemRecord(1, 1, 1, "게이밍 노트북 A15", "공식몰", "RAM:16GB,SSD:1TB", 1, 1720000, 1720000, true),
                            OrderItemRecord(2, 2, 2, "사무용 노트북 Slim", "테크마켓", "색상:실버", 1, 940000, 940000, false),
                        ),
                    createdAt = now.minusSeconds(86400),
                )
            val detail2 =
                OrderDetailRecord(
                    id = 2,
                    userId = 5,
                    orderNumber = "ORD-20260225-A002",
                    status = OrderStatus.PAYMENT_CONFIRMED,
                    totalAmount = 1360000,
                    pointUsed = 0,
                    finalAmount = 1360000,
                    shippingAddress = OrderShippingAddress("김영희", "01023456789", "48058", "부산시 해운대구 센텀중앙로 77", "1203호"),
                    memo = "빠른 배송 요청",
                    items =
                        listOf(
                            OrderItemRecord(3, 3, 1, "미니 데스크탑 Pro", "공식몰", null, 1, 1360000, 1360000, true),
                        ),
                    createdAt = now.minusSeconds(7200),
                )
            return InMemoryOrderRepository(
                seededSummaries =
                    listOf(
                        OrderSummaryRecord(1, 4, detail1.orderNumber, detail1.status, detail1.totalAmount, detail1.pointUsed, detail1.finalAmount, detail1.items.size, detail1.createdAt),
                        OrderSummaryRecord(2, 5, detail2.orderNumber, detail2.status, detail2.totalAmount, detail2.pointUsed, detail2.finalAmount, detail2.items.size, detail2.createdAt),
                    ),
                seededDetails = listOf(detail1, detail2),
            )
        }
    }
}
