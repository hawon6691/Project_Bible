package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

enum class OrderStatus {
    ORDER_PLACED,
    PAYMENT_PENDING,
    PAYMENT_CONFIRMED,
    PREPARING,
    SHIPPING,
    DELIVERED,
    CONFIRMED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURNED,
}

enum class PaymentMethod {
    CARD,
    BANK_TRANSFER,
    VIRTUAL_ACCOUNT,
}

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
}

object CartItemsTable : IntIdTable("cart_items") {
    val user = reference("user_id", UsersTable)
    val product = reference("product_id", ProductsTable)
    val seller = reference("seller_id", SellersTable)
    val selectedOptions = varchar("selected_options", 200).nullable()
    val quantity = integer("quantity")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class CartItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CartItemEntity>(CartItemsTable)

    var userId by CartItemsTable.user
    var productId by CartItemsTable.product
    var sellerId by CartItemsTable.seller
    var selectedOptions by CartItemsTable.selectedOptions
    var quantity by CartItemsTable.quantity
    var createdAt by CartItemsTable.createdAt
    var updatedAt by CartItemsTable.updatedAt
}

object AddressesTable : IntIdTable("addresses") {
    val user = reference("user_id", UsersTable)
    val label = varchar("label", 50)
    val recipientName = varchar("recipient_name", 50)
    val phone = varchar("phone", 20)
    val zipCode = varchar("zip_code", 10)
    val addressLine = varchar("address", 200)
    val addressDetail = varchar("address_detail", 100).nullable()
    val isDefault = bool("is_default")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class AddressEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AddressEntity>(AddressesTable)

    var userId by AddressesTable.user
    var label by AddressesTable.label
    var recipientName by AddressesTable.recipientName
    var phone by AddressesTable.phone
    var zipCode by AddressesTable.zipCode
    var addressLine by AddressesTable.addressLine
    var addressDetail by AddressesTable.addressDetail
    var isDefault by AddressesTable.isDefault
    var createdAt by AddressesTable.createdAt
    var updatedAt by AddressesTable.updatedAt
}

object OrdersTable : IntIdTable("orders") {
    val orderNumber = varchar("order_number", 30)
    val user = reference("user_id", UsersTable)
    val status = pgEnum<OrderStatus>("status", "order_status")
    val totalAmount = integer("total_amount")
    val pointUsed = integer("point_used")
    val finalAmount = integer("final_amount")
    val recipientName = varchar("recipient_name", 50)
    val recipientPhone = varchar("recipient_phone", 20)
    val zipCode = varchar("zip_code", 10)
    val addressLine = varchar("address", 200)
    val addressDetail = varchar("address_detail", 100).nullable()
    val memo = varchar("memo", 200).nullable()
    val version = integer("version")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class OrderEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(OrdersTable)

    var orderNumber by OrdersTable.orderNumber
    var userId by OrdersTable.user
    var status by OrdersTable.status
    var totalAmount by OrdersTable.totalAmount
    var pointUsed by OrdersTable.pointUsed
    var finalAmount by OrdersTable.finalAmount
    var recipientName by OrdersTable.recipientName
    var recipientPhone by OrdersTable.recipientPhone
    var zipCode by OrdersTable.zipCode
    var addressLine by OrdersTable.addressLine
    var addressDetail by OrdersTable.addressDetail
    var memo by OrdersTable.memo
    var version by OrdersTable.version
    var createdAt by OrdersTable.createdAt
    var updatedAt by OrdersTable.updatedAt
}

object OrderItemsTable : IntIdTable("order_items") {
    val order = reference("order_id", OrdersTable)
    val product = reference("product_id", ProductsTable)
    val seller = reference("seller_id", SellersTable)
    val productName = varchar("product_name", 200)
    val sellerName = varchar("seller_name", 100)
    val selectedOptions = varchar("selected_options", 200).nullable()
    val quantity = integer("quantity")
    val unitPrice = integer("unit_price")
    val totalPrice = integer("total_price")
    val isReviewed = bool("is_reviewed")
    val createdAt = timestamp("created_at")
}

class OrderItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderItemEntity>(OrderItemsTable)

    var orderId by OrderItemsTable.order
    var productId by OrderItemsTable.product
    var sellerId by OrderItemsTable.seller
    var productName by OrderItemsTable.productName
    var sellerName by OrderItemsTable.sellerName
    var selectedOptions by OrderItemsTable.selectedOptions
    var quantity by OrderItemsTable.quantity
    var unitPrice by OrderItemsTable.unitPrice
    var totalPrice by OrderItemsTable.totalPrice
    var isReviewed by OrderItemsTable.isReviewed
    var createdAt by OrderItemsTable.createdAt
}

object PaymentsTable : IntIdTable("payments") {
    val order = reference("order_id", OrdersTable)
    val method = pgEnum<PaymentMethod>("method", "payment_method")
    val amount = integer("amount")
    val status = pgEnum<PaymentStatus>("status", "payment_status")
    val paidAt = timestamp("paid_at").nullable()
    val refundedAt = timestamp("refunded_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class PaymentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PaymentEntity>(PaymentsTable)

    var orderId by PaymentsTable.order
    var method by PaymentsTable.method
    var amount by PaymentsTable.amount
    var status by PaymentsTable.status
    var paidAt by PaymentsTable.paidAt
    var refundedAt by PaymentsTable.refundedAt
    var createdAt by PaymentsTable.createdAt
    var updatedAt by PaymentsTable.updatedAt
}
