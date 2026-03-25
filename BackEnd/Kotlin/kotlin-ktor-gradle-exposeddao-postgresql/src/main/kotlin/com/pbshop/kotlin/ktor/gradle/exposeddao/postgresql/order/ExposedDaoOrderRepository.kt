package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderItemEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderItemsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrdersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class ExposedDaoOrderRepository(
    private val databaseFactory: DatabaseFactory,
) : OrderRepository {
    override fun createOrder(
        userId: Int,
        shippingAddress: OrderShippingAddress,
        items: List<NewOrderItem>,
        pointUsed: Int,
        memo: String?,
    ): OrderDetailRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val totalAmount = items.sumOf { it.totalPrice }
            val finalAmount = (totalAmount - pointUsed).coerceAtLeast(0)
            val order =
                OrderEntity.new {
                    orderNumber = buildOrderNumber()
                    this.userId = EntityID(userId, UsersTable)
                    status = OrderStatus.ORDER_PLACED
                    this.totalAmount = totalAmount
                    this.pointUsed = pointUsed
                    this.finalAmount = finalAmount
                    recipientName = shippingAddress.recipientName
                    recipientPhone = shippingAddress.recipientPhone
                    zipCode = shippingAddress.zipCode
                    addressLine = shippingAddress.address
                    addressDetail = shippingAddress.addressDetail
                    this.memo = memo
                    version = 1
                    createdAt = now
                    updatedAt = now
                }
            items.forEach { item ->
                OrderItemEntity.new {
                    orderId = order.id
                    productId = EntityID(item.productId, ProductsTable)
                    sellerId = EntityID(item.sellerId, SellersTable)
                    productName = item.productName
                    sellerName = item.sellerName
                    selectedOptions = item.selectedOptions
                    quantity = item.quantity
                    unitPrice = item.unitPrice
                    totalPrice = item.totalPrice
                    isReviewed = false
                    createdAt = now
                }
            }
            requireNotNull(findOrderDetailById(userId, order.id.value))
        }

    override fun listOrders(
        userId: Int,
        page: Int,
        limit: Int,
        status: OrderStatus?,
    ): OrderListResult =
        databaseFactory.withTransaction {
            val rows =
                OrdersTable
                    .selectAll()
                    .where {
                        if (status == null) {
                            OrdersTable.user eq userId
                        } else {
                            (OrdersTable.user eq userId) and (OrdersTable.status eq status)
                        }
                    }.orderBy(OrdersTable.createdAt to SortOrder.DESC, OrdersTable.id to SortOrder.DESC)
                    .map(::toSummary)
            val offset = (page - 1) * limit
            OrderListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findOrderDetailById(
        userId: Int,
        orderId: Int,
    ): OrderDetailRecord? =
        databaseFactory.withTransaction {
            val order =
                OrdersTable.selectAll()
                    .where { (OrdersTable.id eq orderId) and (OrdersTable.user eq userId) }
                    .limit(1)
                    .firstOrNull()
                    ?: return@withTransaction null
            toDetail(order)
        }

    override fun cancelOrder(
        userId: Int,
        orderId: Int,
    ): OrderDetailRecord =
        databaseFactory.withTransaction {
            val entity = OrderEntity.findById(orderId)?.takeIf { it.userId.value == userId } ?: error("Order $orderId not found")
            entity.status = OrderStatus.CANCELLED
            entity.updatedAt = Instant.now()
            requireNotNull(findOrderDetailById(userId, orderId))
        }

    override fun listAdminOrders(
        page: Int,
        limit: Int,
        status: OrderStatus?,
    ): OrderListResult =
        databaseFactory.withTransaction {
            val rows =
                OrdersTable
                    .selectAll()
                    .let { query ->
                        if (status == null) {
                            query
                        } else {
                            query.where { OrdersTable.status eq status }
                        }
                    }
                    .orderBy(OrdersTable.createdAt to SortOrder.DESC, OrdersTable.id to SortOrder.DESC)
                    .map(::toSummary)
            val offset = (page - 1) * limit
            OrderListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun updateOrderStatus(
        orderId: Int,
        status: OrderStatus,
    ): OrderDetailRecord =
        databaseFactory.withTransaction {
            val entity = OrderEntity.findById(orderId) ?: error("Order $orderId not found")
            entity.status = status
            entity.updatedAt = Instant.now()
            requireNotNull(findOrderDetailById(entity.userId.value, orderId))
        }

    private fun toSummary(row: org.jetbrains.exposed.sql.ResultRow): OrderSummaryRecord {
        val itemCount =
            OrderItemsTable.selectAll()
                .where { OrderItemsTable.order eq row[OrdersTable.id].value }
                .count()
                .toInt()
        return OrderSummaryRecord(
            id = row[OrdersTable.id].value,
            userId = row[OrdersTable.user].value,
            orderNumber = row[OrdersTable.orderNumber],
            status = row[OrdersTable.status],
            totalAmount = row[OrdersTable.totalAmount],
            pointUsed = row[OrdersTable.pointUsed],
            finalAmount = row[OrdersTable.finalAmount],
            itemCount = itemCount,
            createdAt = row[OrdersTable.createdAt],
        )
    }

    private fun toDetail(row: org.jetbrains.exposed.sql.ResultRow): OrderDetailRecord {
        val orderId = row[OrdersTable.id].value
        val items =
            OrderItemsTable
                .selectAll()
                .where { OrderItemsTable.order eq orderId }
                .orderBy(OrderItemsTable.id to SortOrder.ASC)
                .map {
                    OrderItemRecord(
                        id = it[OrderItemsTable.id].value,
                        productId = it[OrderItemsTable.product].value,
                        sellerId = it[OrderItemsTable.seller].value,
                        productName = it[OrderItemsTable.productName],
                        sellerName = it[OrderItemsTable.sellerName],
                        selectedOptions = it[OrderItemsTable.selectedOptions],
                        quantity = it[OrderItemsTable.quantity],
                        unitPrice = it[OrderItemsTable.unitPrice],
                        totalPrice = it[OrderItemsTable.totalPrice],
                        isReviewed = it[OrderItemsTable.isReviewed],
                    )
                }
        return OrderDetailRecord(
            id = orderId,
            userId = row[OrdersTable.user].value,
            orderNumber = row[OrdersTable.orderNumber],
            status = row[OrdersTable.status],
            totalAmount = row[OrdersTable.totalAmount],
            pointUsed = row[OrdersTable.pointUsed],
            finalAmount = row[OrdersTable.finalAmount],
            shippingAddress =
                OrderShippingAddress(
                    recipientName = row[OrdersTable.recipientName],
                    recipientPhone = row[OrdersTable.recipientPhone],
                    zipCode = row[OrdersTable.zipCode],
                    address = row[OrdersTable.addressLine],
                    addressDetail = row[OrdersTable.addressDetail],
                ),
            memo = row[OrdersTable.memo],
            items = items,
            createdAt = row[OrdersTable.createdAt],
        )
    }

    private fun buildOrderNumber(): String {
        val datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val suffix = Random.nextInt(10000, 99999)
        return "ORD-$datePart-$suffix"
    }
}
