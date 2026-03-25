package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoSellerRepository(
    private val databaseFactory: DatabaseFactory,
) : SellerRepository {
    override fun listSellers(
        page: Int,
        limit: Int,
    ): SellerListResult =
        databaseFactory.withTransaction {
            val totalCount = SellersTable.select(SellersTable.id.count()).single()[SellersTable.id.count()].toInt()
            val items =
                SellersTable
                    .selectAll()
                    .orderBy(SellersTable.id to SortOrder.ASC)
                    .limit(limit, ((page - 1) * limit).toLong())
                    .map(::toRecord)
            SellerListResult(items, totalCount)
        }

    override fun findSellerById(id: Int): SellerRecord? =
        databaseFactory.withTransaction {
            SellersTable.selectAll().where { SellersTable.id eq id }.limit(1).firstOrNull()?.let(::toRecord)
        }

    override fun createSeller(newSeller: NewSeller): SellerRecord =
        databaseFactory.withTransaction {
            val id =
                SellersTable.insertAndGetId {
                    it[name] = newSeller.name
                    it[url] = newSeller.url
                    it[logoUrl] = newSeller.logoUrl
                    it[trustScore] = newSeller.trustScore
                    it[trustGrade] = newSeller.trustGrade
                    it[description] = newSeller.description
                    it[isActive] = newSeller.isActive
                    it[updatedAt] = Instant.now()
                }.value
            requireNotNull(findSellerById(id))
        }

    override fun updateSeller(
        id: Int,
        update: SellerUpdate,
    ): SellerRecord =
        databaseFactory.withTransaction {
            val current = requireNotNull(findSellerById(id)) { "Seller $id not found" }
            SellersTable.update({ SellersTable.id eq id }) {
                it[name] = update.name ?: current.name
                it[url] = update.url ?: current.url
                it[logoUrl] = update.logoUrl ?: current.logoUrl
                it[trustScore] = update.trustScore ?: current.trustScore
                it[trustGrade] = update.trustGrade ?: current.trustGrade
                it[description] = update.description ?: current.description
                it[isActive] = update.isActive ?: current.isActive
                it[updatedAt] = Instant.now()
            }
            requireNotNull(findSellerById(id))
        }

    override fun deleteSeller(id: Int) {
        databaseFactory.withTransaction {
            SellersTable.deleteWhere { SellersTable.id eq id }
        }
    }

    override fun hasLinkedPriceEntries(id: Int): Boolean =
        databaseFactory.withTransaction {
            !PriceEntriesTable.selectAll().where { PriceEntriesTable.seller eq id }.limit(1).empty()
        }

    private fun toRecord(row: ResultRow): SellerRecord =
        SellerRecord(
            id = row[SellersTable.id].value,
            name = row[SellersTable.name],
            url = row[SellersTable.url],
            logoUrl = row[SellersTable.logoUrl],
            trustScore = row[SellersTable.trustScore],
            trustGrade = row[SellersTable.trustGrade],
            description = row[SellersTable.description],
            isActive = row[SellersTable.isActive],
            createdAt = row[SellersTable.createdAt],
        )
}
