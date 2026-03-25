package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceAlertsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceHistoryTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ShippingType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ExposedDaoPriceRepository(
    private val databaseFactory: DatabaseFactory,
) : PriceRepository {
    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.limit(1).empty()
        }

    override fun sellerExists(sellerId: Int): Boolean =
        databaseFactory.withTransaction {
            !SellersTable.selectAll().where { SellersTable.id eq sellerId }.limit(1).empty()
        }

    override fun listProductPrices(productId: Int): List<PriceEntryRecord> =
        databaseFactory.withTransaction {
            PriceEntriesTable
                .innerJoin(SellersTable)
                .innerJoin(ProductsTable)
                .selectAll()
                .where { PriceEntriesTable.product eq productId }
                .orderBy(PriceEntriesTable.price to SortOrder.ASC, SellersTable.id to SortOrder.ASC)
                .map(::toPriceEntryRecord)
        }

    override fun findPriceEntryById(id: Int): PriceEntryRecord? =
        databaseFactory.withTransaction {
            PriceEntriesTable
                .innerJoin(SellersTable)
                .innerJoin(ProductsTable)
                .selectAll()
                .where { PriceEntriesTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let(::toPriceEntryRecord)
        }

    override fun createPriceEntry(
        productId: Int,
        newEntry: NewPriceEntry,
    ): PriceEntryRecord =
        databaseFactory.withTransaction {
            val id =
                PriceEntriesTable.insertAndGetId {
                    it[product] = productId
                    it[seller] = newEntry.sellerId
                    it[price] = newEntry.price
                    it[shippingCost] = newEntry.shippingCost
                    it[shippingInfo] = newEntry.shippingInfo
                    it[productUrl] = newEntry.productUrl
                    it[shippingFee] = newEntry.shippingFee
                    it[shippingType] = enumValueOf<ShippingType>(newEntry.shippingType)
                    it[clickCount] = 0
                    it[isAvailable] = newEntry.isAvailable
                }.value
            requireNotNull(findPriceEntryById(id))
        }

    override fun updatePriceEntry(
        id: Int,
        update: PriceEntryUpdate,
    ): PriceEntryRecord =
        databaseFactory.withTransaction {
            val current = requireNotNull(findPriceEntryById(id)) { "Price entry $id not found" }
            PriceEntriesTable.update({ PriceEntriesTable.id eq id }) {
                it[seller] = update.sellerId ?: current.sellerId
                it[price] = update.price ?: current.price
                it[shippingCost] = update.shippingCost ?: current.shippingCost
                it[shippingInfo] = update.shippingInfo ?: current.shippingInfo
                it[productUrl] = update.productUrl ?: current.productUrl
                it[shippingFee] = update.shippingFee ?: current.shippingFee
                it[shippingType] = update.shippingType?.let { enumValueOf<ShippingType>(it) } ?: enumValueOf<ShippingType>(current.shippingType)
                it[isAvailable] = update.isAvailable ?: current.isAvailable
            }
            requireNotNull(findPriceEntryById(id))
        }

    override fun deletePriceEntry(id: Int) {
        databaseFactory.withTransaction {
            PriceEntriesTable.deleteWhere { PriceEntriesTable.id eq id }
        }
    }

    override fun listPriceHistory(productId: Int): List<PriceHistoryRecord> =
        databaseFactory.withTransaction {
            PriceHistoryTable
                .selectAll()
                .where { PriceHistoryTable.product eq productId }
                .orderBy(PriceHistoryTable.date to SortOrder.DESC)
                .map {
                    PriceHistoryRecord(
                        date = it[PriceHistoryTable.date],
                        lowestPrice = it[PriceHistoryTable.lowestPrice],
                        averagePrice = it[PriceHistoryTable.averagePrice],
                        highestPrice = it[PriceHistoryTable.highestPrice],
                    )
                }
        }

    override fun listPriceAlerts(
        userId: Int,
        page: Int,
        limit: Int,
    ): PriceAlertListResult =
        databaseFactory.withTransaction {
            val totalCount = PriceAlertsTable.select(PriceAlertsTable.id.count()).where { PriceAlertsTable.user eq userId }.single()[PriceAlertsTable.id.count()].toInt()
            val items =
                PriceAlertsTable
                    .innerJoin(ProductsTable)
                    .selectAll()
                    .where { PriceAlertsTable.user eq userId }
                    .orderBy(PriceAlertsTable.id to SortOrder.DESC)
                    .limit(limit, ((page - 1) * limit).toLong())
                    .map(::toPriceAlertRecord)
            PriceAlertListResult(items, totalCount)
        }

    override fun createPriceAlert(
        userId: Int,
        newAlert: NewPriceAlert,
    ): PriceAlertRecord =
        databaseFactory.withTransaction {
            val id =
                PriceAlertsTable.insertAndGetId {
                    it[user] = userId
                    it[product] = newAlert.productId
                    it[targetPrice] = newAlert.targetPrice
                    it[isTriggered] = false
                    it[isActive] = newAlert.isActive
                }.value
            listPriceAlerts(userId, 1, 100).items.first { it.id == id }
        }

    private fun toPriceEntryRecord(row: ResultRow): PriceEntryRecord =
        PriceEntryRecord(
            id = row[PriceEntriesTable.id].value,
            productId = row[PriceEntriesTable.product].value,
            productName = row[ProductsTable.name],
            sellerId = row[PriceEntriesTable.seller].value,
            sellerName = row[SellersTable.name],
            sellerLogoUrl = row[SellersTable.logoUrl],
            trustScore = row[SellersTable.trustScore],
            price = row[PriceEntriesTable.price],
            shippingCost = row[PriceEntriesTable.shippingCost],
            shippingInfo = row[PriceEntriesTable.shippingInfo],
            productUrl = row[PriceEntriesTable.productUrl],
            shippingFee = row[PriceEntriesTable.shippingFee],
            shippingType = row[PriceEntriesTable.shippingType].name,
            totalPrice = row[PriceEntriesTable.totalPrice],
            clickCount = row[PriceEntriesTable.clickCount],
            isAvailable = row[PriceEntriesTable.isAvailable],
            crawledAt = row[PriceEntriesTable.crawledAt],
        )

    private fun toPriceAlertRecord(row: ResultRow): PriceAlertRecord =
        PriceAlertRecord(
            id = row[PriceAlertsTable.id].value,
            userId = row[PriceAlertsTable.user].value,
            productId = row[PriceAlertsTable.product].value,
            productName = row[ProductsTable.name],
            targetPrice = row[PriceAlertsTable.targetPrice],
            isTriggered = row[PriceAlertsTable.isTriggered],
            triggeredAt = row[PriceAlertsTable.triggeredAt],
            isActive = row[PriceAlertsTable.isActive],
            createdAt = row[PriceAlertsTable.createdAt],
        )
}
