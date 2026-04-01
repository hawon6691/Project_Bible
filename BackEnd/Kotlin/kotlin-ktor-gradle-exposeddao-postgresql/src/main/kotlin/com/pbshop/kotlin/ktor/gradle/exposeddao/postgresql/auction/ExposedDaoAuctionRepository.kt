package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.AuctionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.BidsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoAuctionRepository(
    private val databaseFactory: DatabaseFactory,
) : AuctionRepository {
    override fun categoryExists(categoryId: Int): Boolean =
        databaseFactory.withTransaction { !CategoriesTable.selectAll().where { CategoriesTable.id eq categoryId }.empty() }

    override fun sellerExists(sellerId: Int): Boolean =
        databaseFactory.withTransaction { !SellersTable.selectAll().where { SellersTable.id eq sellerId }.empty() }

    override fun createAuction(ownerId: Int, input: NewAuction): AuctionDetailRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val id =
                AuctionsTable.insert {
                    it[user] = EntityID(ownerId, UsersTable)
                    it[category] = input.categoryId?.let { categoryId -> EntityID(categoryId, CategoriesTable) }
                    it[title] = input.title
                    it[description] = input.description
                    it[specsJson] = input.specsJson
                    it[budget] = input.budget
                    it[status] = AuctionStatus.OPEN.name
                    it[bidCount] = 0
                    it[selectedBidId] = null
                    it[expiresAt] = input.expiresAt
                    it[createdAt] = now
                    it[updatedAt] = now
                } get AuctionsTable.id
            requireNotNull(findAuctionById(id.value))
        }

    override fun listAuctions(status: AuctionStatus?, categoryId: Int?, page: Int, limit: Int): AuctionPageResult =
        databaseFactory.withTransaction {
            val rows =
                AuctionsTable.selectAll()
                    .where {
                        var predicate: Op<Boolean> = Op.TRUE
                        if (status != null) predicate = predicate and (AuctionsTable.status eq status.name)
                        if (categoryId != null) predicate = predicate and (AuctionsTable.category eq categoryId)
                        predicate
                    }.orderBy(AuctionsTable.createdAt to SortOrder.DESC)
                    .map(::toSummary)
            val from = ((page - 1) * limit).coerceAtMost(rows.size)
            val to = (from + limit).coerceAtMost(rows.size)
            AuctionPageResult(rows.subList(from, to), rows.size)
        }

    override fun findAuctionById(id: Int): AuctionDetailRecord? =
        databaseFactory.withTransaction {
            val auctionRow = AuctionsTable.selectAll().where { AuctionsTable.id eq id }.singleOrNull() ?: return@withTransaction null
            val bids =
                BidsTable.selectAll()
                    .where { BidsTable.auction eq id }
                    .orderBy(BidsTable.price to SortOrder.ASC, BidsTable.createdAt to SortOrder.ASC)
                    .map(::toBid)
            toDetail(auctionRow, bids)
        }

    override fun createBid(sellerId: Int, auctionId: Int, input: NewAuctionBid): AuctionBidRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val bidId =
                BidsTable.insert {
                    it[auction] = EntityID(auctionId, AuctionsTable)
                    it[seller] = EntityID(sellerId, SellersTable)
                    it[price] = input.price
                    it[description] = input.description
                    it[deliveryDays] = input.deliveryDays
                    it[status] = AuctionBidStatus.ACTIVE.name
                    it[createdAt] = now
                    it[updatedAt] = now
                } get BidsTable.id
            AuctionsTable.update({ AuctionsTable.id eq auctionId }) {
                with(org.jetbrains.exposed.sql.SqlExpressionBuilder) {
                    it.update(bidCount, bidCount + 1)
                }
                it[updatedAt] = now
            }
            BidsTable.selectAll().where { BidsTable.id eq bidId.value }.single().let(::toBid)
        }

    override fun updateBid(auctionId: Int, bidId: Int, update: AuctionBidUpdate): AuctionBidRecord? =
        databaseFactory.withTransaction {
            val exists = BidsTable.selectAll().where { (BidsTable.id eq bidId) and (BidsTable.auction eq auctionId) }.singleOrNull() ?: return@withTransaction null
            BidsTable.update({ (BidsTable.id eq bidId) and (BidsTable.auction eq auctionId) }) {
                update.price?.let { value -> it[BidsTable.price] = value }
                if (update.description != null) {
                    it[description] = update.description.ifBlank { null }
                }
                update.deliveryDays?.let { value -> it[deliveryDays] = value }
                it[updatedAt] = Instant.now()
            }
            BidsTable.selectAll().where { BidsTable.id eq exists[BidsTable.id].value }.single().let(::toBid)
        }

    override fun deleteBid(auctionId: Int, bidId: Int) {
        databaseFactory.withTransaction {
            BidsTable.deleteWhere { (BidsTable.id eq bidId) and (BidsTable.auction eq auctionId) }
            val count = BidsTable.selectAll().where { BidsTable.auction eq auctionId }.count().toInt()
            AuctionsTable.update({ AuctionsTable.id eq auctionId }) {
                it[bidCount] = count
                it[updatedAt] = Instant.now()
            }
        }
    }

    override fun selectBid(auctionId: Int, bidId: Int) {
        databaseFactory.withTransaction {
            BidsTable.update({ (BidsTable.auction eq auctionId) and (BidsTable.id eq bidId) }) {
                it[status] = AuctionBidStatus.SELECTED.name
                it[updatedAt] = Instant.now()
            }
            AuctionsTable.update({ AuctionsTable.id eq auctionId }) {
                it[status] = AuctionStatus.CLOSED.name
                it[selectedBidId] = bidId
                it[updatedAt] = Instant.now()
            }
        }
    }

    override fun cancelAuction(auctionId: Int) {
        databaseFactory.withTransaction {
            AuctionsTable.update({ AuctionsTable.id eq auctionId }) {
                it[status] = AuctionStatus.CANCELLED.name
                it[updatedAt] = Instant.now()
            }
        }
    }

    private fun toSummary(row: ResultRow): AuctionSummaryRecord =
        AuctionSummaryRecord(
            id = row[AuctionsTable.id].value,
            ownerId = row[AuctionsTable.user].value,
            title = row[AuctionsTable.title],
            categoryId = row[AuctionsTable.category]?.value,
            budget = row[AuctionsTable.budget],
            status = AuctionStatus.valueOf(row[AuctionsTable.status]),
            bidCount = row[AuctionsTable.bidCount],
            createdAt = row[AuctionsTable.createdAt],
        )

    private fun toDetail(row: ResultRow, bids: List<AuctionBidRecord>): AuctionDetailRecord =
        AuctionDetailRecord(
            id = row[AuctionsTable.id].value,
            ownerId = row[AuctionsTable.user].value,
            title = row[AuctionsTable.title],
            description = row[AuctionsTable.description],
            categoryId = row[AuctionsTable.category]?.value,
            specsJson = row[AuctionsTable.specsJson],
            budget = row[AuctionsTable.budget],
            status = AuctionStatus.valueOf(row[AuctionsTable.status]),
            bidCount = row[AuctionsTable.bidCount],
            selectedBidId = row[AuctionsTable.selectedBidId],
            expiresAt = row[AuctionsTable.expiresAt],
            bids = bids,
            createdAt = row[AuctionsTable.createdAt],
            updatedAt = row[AuctionsTable.updatedAt],
        )

    private fun toBid(row: ResultRow): AuctionBidRecord =
        AuctionBidRecord(
            id = row[BidsTable.id].value,
            auctionId = row[BidsTable.auction].value,
            sellerId = row[BidsTable.seller].value,
            price = row[BidsTable.price],
            description = row[BidsTable.description],
            deliveryDays = row[BidsTable.deliveryDays],
            status = AuctionBidStatus.valueOf(row[BidsTable.status]),
            createdAt = row[BidsTable.createdAt],
            updatedAt = row[BidsTable.updatedAt],
        )
}
