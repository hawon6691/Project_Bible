package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.WishlistEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.WishlistsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoWishlistRepository(
    private val databaseFactory: DatabaseFactory,
) : WishlistRepository {
    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { ProductsTable.id eq productId }.limit(1).empty()
        }

    override fun listWishlist(
        userId: Int,
        page: Int,
        limit: Int,
    ): WishlistListResult =
        databaseFactory.withTransaction {
            val filtered =
                WishlistsTable
                    .innerJoin(ProductsTable)
                    .selectAll()
                    .where { WishlistsTable.user eq userId }
                    .orderBy(WishlistsTable.createdAt to SortOrder.DESC, WishlistsTable.id to SortOrder.DESC)
                    .map(::toRecord)
            val offset = (page - 1) * limit
            WishlistListResult(filtered.drop(offset).take(limit), filtered.size)
        }

    override fun findWishlistItem(
        userId: Int,
        productId: Int,
    ): WishlistItemRecord? =
        databaseFactory.withTransaction {
            WishlistsTable
                .innerJoin(ProductsTable)
                .selectAll()
                .where { (WishlistsTable.user eq userId) and (WishlistsTable.product eq productId) }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun createWishlistItem(
        userId: Int,
        productId: Int,
    ): WishlistItemRecord =
        databaseFactory.withTransaction {
            val created =
                WishlistEntity.new {
                    this.userId = EntityID(userId, UsersTable)
                    this.productId = EntityID(productId, ProductsTable)
                    createdAt = Instant.now()
                }
            requireNotNull(findWishlistItem(userId, created.productId.value))
        }

    override fun deleteWishlistItem(
        userId: Int,
        productId: Int,
    ) {
        databaseFactory.withTransaction {
            WishlistEntity.find { (WishlistsTable.user eq userId) and (WishlistsTable.product eq productId) }
                .forEach { it.delete() }
        }
    }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): WishlistItemRecord =
        WishlistItemRecord(
            id = row[WishlistsTable.id].value,
            userId = row[WishlistsTable.user].value,
            productId = row[WishlistsTable.product].value,
            productName = row[ProductsTable.name],
            thumbnailUrl = row[ProductsTable.thumbnailUrl],
            lowestPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
            wishlistedAt = row[WishlistsTable.createdAt],
        )
}
