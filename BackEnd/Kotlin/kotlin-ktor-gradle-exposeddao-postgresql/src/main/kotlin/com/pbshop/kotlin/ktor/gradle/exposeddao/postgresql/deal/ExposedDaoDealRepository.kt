package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.DealProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.DealsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class ExposedDaoDealRepository(
    private val databaseFactory: DatabaseFactory,
) : DealRepository {
    private val metaByDealId = ConcurrentHashMap<Int, DealMeta>()

    override fun listDeals(
        type: String?,
        page: Int,
        limit: Int,
    ): DealListResult =
        databaseFactory.withTransaction {
            val rows =
                DealsTable.selectAll()
                    .orderBy(DealsTable.startAt to SortOrder.DESC, DealsTable.id to SortOrder.DESC)
                    .map(::toDealRecord)
                    .filter { type == null || it.type.equals(type, true) }
            val offset = (page - 1) * limit
            DealListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findDealById(id: Int): DealRecord? =
        databaseFactory.withTransaction {
            DealsTable.selectAll().where { DealsTable.id eq id }.limit(1).firstOrNull()?.let(::toDealRecord)
        }

    override fun createDeal(newDeal: NewDeal): DealRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val insertedId =
                DealsTable.insertAndGetId { row ->
                    row[product] = EntityID(newDeal.productId, ProductsTable)
                    row[title] = newDeal.title
                    row[description] = newDeal.description
                    row[discountRate] = newDeal.discountRate
                    row[startAt] = newDeal.startAt
                    row[endAt] = newDeal.endAt
                    row[isActive] = newDeal.isActive
                    row[createdAt] = now
                    row[updatedAt] = now
                }.value
            metaByDealId[insertedId] = DealMeta(newDeal.type, newDeal.bannerUrl)
            newDeal.products.forEach { product ->
                DealProductsTable.insert { row ->
                    row[deal] = EntityID(insertedId, DealsTable)
                    row[DealProductsTable.product] = EntityID(product.productId, ProductsTable)
                    row[dealPrice] = product.dealPrice
                    row[stock] = product.stock
                    row[soldCount] = 0
                    row[createdAt] = now
                }
            }
            requireNotNull(findDealById(insertedId))
        }

    override fun updateDeal(
        id: Int,
        update: DealUpdate,
    ): DealRecord =
        databaseFactory.withTransaction {
            val existing = DealsTable.selectAll().where { DealsTable.id eq id }.limit(1).firstOrNull() ?: error("Deal $id not found")
            DealsTable.update({ DealsTable.id eq id }) { row ->
                row[title] = update.title ?: existing[DealsTable.title]
                row[description] = update.description ?: existing[DealsTable.description]
                row[discountRate] = update.discountRate ?: existing[DealsTable.discountRate]
                row[startAt] = update.startAt ?: existing[DealsTable.startAt]
                row[endAt] = update.endAt ?: existing[DealsTable.endAt]
                row[isActive] = update.isActive ?: existing[DealsTable.isActive]
                row[updatedAt] = Instant.now()
            }
            val currentMeta = metaByDealId[id] ?: DealMeta(type = "SPECIAL", bannerUrl = null)
            metaByDealId[id] = DealMeta(update.type ?: currentMeta.type, update.bannerUrl ?: currentMeta.bannerUrl)
            if (update.products != null) {
                DealProductsTable.deleteWhere { DealProductsTable.deal eq id }
                update.products.forEach { product ->
                    DealProductsTable.insert { row ->
                        row[deal] = EntityID(id, DealsTable)
                        row[DealProductsTable.product] = EntityID(product.productId, ProductsTable)
                        row[dealPrice] = product.dealPrice
                        row[stock] = product.stock
                        row[soldCount] = 0
                        row[createdAt] = Instant.now()
                    }
                }
            }
            requireNotNull(findDealById(id))
        }

    override fun deleteDeal(id: Int) {
        databaseFactory.withTransaction {
            DealProductsTable.deleteWhere { DealProductsTable.deal eq id }
            DealsTable.deleteWhere { DealsTable.id eq id }
            metaByDealId.remove(id)
        }
    }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.limit(1).any()
        }

    private fun toDealRecord(row: org.jetbrains.exposed.sql.ResultRow): DealRecord {
        val dealId = row[DealsTable.id].value
        val products =
            DealProductsTable.innerJoin(ProductsTable)
                .selectAll()
                .where { DealProductsTable.deal eq dealId }
                .orderBy(DealProductsTable.id to SortOrder.ASC)
                .map {
                    DealProductRecord(
                        id = it[DealProductsTable.id].value,
                        productId = it[DealProductsTable.product].value,
                        productName = it[ProductsTable.name],
                        dealPrice = it[DealProductsTable.dealPrice],
                        stock = it[DealProductsTable.stock],
                        soldCount = it[DealProductsTable.soldCount],
                    )
                }
        val meta = metaByDealId[dealId] ?: DealMeta(type = "SPECIAL", bannerUrl = null)
        return DealRecord(
            id = dealId,
            productId = row[DealsTable.product].value,
            title = row[DealsTable.title],
            description = row[DealsTable.description],
            type = meta.type,
            discountRate = row[DealsTable.discountRate],
            startAt = row[DealsTable.startAt],
            endAt = row[DealsTable.endAt],
            isActive = row[DealsTable.isActive],
            bannerUrl = meta.bannerUrl,
            createdAt = row[DealsTable.createdAt],
            updatedAt = row[DealsTable.updatedAt],
            products = products,
        )
    }

    private data class DealMeta(
        val type: String,
        val bannerUrl: String?,
    )
}
