package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

enum class PointType {
    EARN,
    USE,
    REFUND,
    EXPIRE,
    ADMIN_GRANT,
}

object ReviewsTable : IntIdTable("reviews") {
    val user = reference("user_id", UsersTable)
    val product = reference("product_id", ProductsTable)
    val order = reference("order_id", OrdersTable)
    val rating = short("rating")
    val content = text("content")
    val isBest = bool("is_best")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class ReviewEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ReviewEntity>(ReviewsTable)

    var userId by ReviewsTable.user
    var productId by ReviewsTable.product
    var orderId by ReviewsTable.order
    var rating by ReviewsTable.rating
    var content by ReviewsTable.content
    var isBest by ReviewsTable.isBest
    var createdAt by ReviewsTable.createdAt
    var updatedAt by ReviewsTable.updatedAt
    var deletedAt by ReviewsTable.deletedAt
}

object WishlistsTable : IntIdTable("wishlists") {
    val user = reference("user_id", UsersTable)
    val product = reference("product_id", ProductsTable)
    val createdAt = timestamp("created_at")
}

class WishlistEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WishlistEntity>(WishlistsTable)

    var userId by WishlistsTable.user
    var productId by WishlistsTable.product
    var createdAt by WishlistsTable.createdAt
}

object PointTransactionsTable : IntIdTable("point_transactions") {
    val user = reference("user_id", UsersTable)
    val type = pgEnum<PointType>("type", "point_type")
    val amount = integer("amount")
    val balance = integer("balance")
    val description = varchar("description", 200)
    val referenceType = varchar("reference_type", 50).nullable()
    val referenceId = integer("reference_id").nullable()
    val expiresAt = timestamp("expires_at").nullable()
    val createdAt = timestamp("created_at")
}

class PointTransactionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PointTransactionEntity>(PointTransactionsTable)

    var userId by PointTransactionsTable.user
    var type by PointTransactionsTable.type
    var amount by PointTransactionsTable.amount
    var balance by PointTransactionsTable.balance
    var description by PointTransactionsTable.description
    var referenceType by PointTransactionsTable.referenceType
    var referenceId by PointTransactionsTable.referenceId
    var expiresAt by PointTransactionsTable.expiresAt
    var createdAt by PointTransactionsTable.createdAt
}
