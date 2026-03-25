package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CartItemEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CartItemsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoCartRepository(
    private val databaseFactory: DatabaseFactory,
) : CartRepository {
    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { ProductsTable.id eq productId }.limit(1).empty()
        }

    override fun sellerExists(sellerId: Int): Boolean =
        databaseFactory.withTransaction {
            !SellersTable.selectAll().where { SellersTable.id eq sellerId }.limit(1).empty()
        }

    override fun listCartItems(userId: Int): List<CartItemRecord> =
        databaseFactory.withTransaction {
            CartItemsTable
                .innerJoin(ProductsTable)
                .innerJoin(SellersTable)
                .selectAll()
                .where { CartItemsTable.user eq userId }
                .orderBy(CartItemsTable.id to SortOrder.ASC)
                .map(::toRecord)
        }

    override fun findCartItemById(
        userId: Int,
        itemId: Int,
    ): CartItemRecord? =
        databaseFactory.withTransaction {
            CartItemsTable
                .innerJoin(ProductsTable)
                .innerJoin(SellersTable)
                .selectAll()
                .where { (CartItemsTable.id eq itemId) and (CartItemsTable.user eq userId) }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun addCartItem(
        userId: Int,
        newItem: NewCartItem,
    ): CartItemRecord =
        databaseFactory.withTransaction {
            val existingId =
                CartItemsTable
                    .selectAll()
                    .where {
                        (CartItemsTable.user eq userId) and
                            (CartItemsTable.product eq newItem.productId) and
                            (CartItemsTable.seller eq newItem.sellerId) and
                            selectedOptionsPredicate(newItem.selectedOptions)
                    }.limit(1)
                    .firstOrNull()
                    ?.get(CartItemsTable.id)
                    ?.value

            val entity =
                if (existingId != null) {
                    requireNotNull(CartItemEntity.findById(existingId)).apply {
                        quantity += newItem.quantity
                        updatedAt = Instant.now()
                    }
                } else {
                    CartItemEntity.new {
                        this.userId = EntityID(userId, UsersTable)
                        this.productId = EntityID(newItem.productId, ProductsTable)
                        this.sellerId = EntityID(newItem.sellerId, SellersTable)
                        this.selectedOptions = newItem.selectedOptions
                        quantity = newItem.quantity
                        createdAt = Instant.now()
                        updatedAt = Instant.now()
                    }
                }

            requireNotNull(findCartItemById(userId, entity.id.value))
        }

    override fun updateCartItemQuantity(
        userId: Int,
        itemId: Int,
        quantity: Int,
    ): CartItemRecord =
        databaseFactory.withTransaction {
            val entity = CartItemEntity.findById(itemId)?.takeIf { it.userId.value == userId } ?: error("Cart item $itemId not found")
            entity.quantity = quantity
            entity.updatedAt = Instant.now()
            requireNotNull(findCartItemById(userId, itemId))
        }

    override fun deleteCartItem(
        userId: Int,
        itemId: Int,
    ) {
        databaseFactory.withTransaction {
            CartItemEntity.findById(itemId)?.takeIf { it.userId.value == userId }?.delete() ?: error("Cart item $itemId not found")
        }
    }

    override fun clearCart(userId: Int) {
        databaseFactory.withTransaction {
            CartItemEntity.find { CartItemsTable.user eq userId }.forEach { it.delete() }
        }
    }

    private fun toRecord(row: ResultRow): CartItemRecord {
        val unitPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price]
        val quantity = row[CartItemsTable.quantity]
        return CartItemRecord(
            id = row[CartItemsTable.id].value,
            userId = row[CartItemsTable.user].value,
            productId = row[CartItemsTable.product].value,
            sellerId = row[CartItemsTable.seller].value,
            productName = row[ProductsTable.name],
            sellerName = row[SellersTable.name],
            thumbnailUrl = row[ProductsTable.thumbnailUrl],
            selectedOptions = row[CartItemsTable.selectedOptions],
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = unitPrice * quantity,
            createdAt = row[CartItemsTable.createdAt],
        )
    }

    private fun selectedOptionsPredicate(selectedOptions: String?) =
        if (selectedOptions == null) {
            CartItemsTable.selectedOptions.isNull()
        } else {
            CartItemsTable.selectedOptions eq selectedOptions
        }
}
