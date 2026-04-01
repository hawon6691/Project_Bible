package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FraudAlertsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoFraudRepository(
    private val databaseFactory: DatabaseFactory,
) : FraudRepository {
    override fun listAlerts(status: FraudAlertStatus?, page: Int, limit: Int): FraudAlertPageResult =
        databaseFactory.withTransaction {
            val query = FraudAlertsTable.selectAll()
            val filtered = if (status == null) query else query.where { FraudAlertsTable.status eq status.name }
            val rows = filtered.orderBy(FraudAlertsTable.createdAt to SortOrder.DESC).map(::toRecord)
            val from = ((page - 1) * limit).coerceAtMost(rows.size)
            val to = (from + limit).coerceAtMost(rows.size)
            FraudAlertPageResult(rows.subList(from, to), rows.size)
        }

    override fun findAlertById(id: Int): FraudAlertRecord? =
        databaseFactory.withTransaction {
            FraudAlertsTable.selectAll().where { FraudAlertsTable.id eq id }.singleOrNull()?.let(::toRecord)
        }

    override fun approveAlert(id: Int, adminUserId: Int) {
        databaseFactory.withTransaction {
            FraudAlertsTable.update({ FraudAlertsTable.id eq id }) {
                it[status] = FraudAlertStatus.APPROVED.name
                it[reviewedBy] = org.jetbrains.exposed.dao.id.EntityID(adminUserId, com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable)
                it[updatedAt] = Instant.now()
            }
        }
    }

    override fun rejectAlert(id: Int, adminUserId: Int) {
        databaseFactory.withTransaction {
            FraudAlertsTable.update({ FraudAlertsTable.id eq id }) {
                it[status] = FraudAlertStatus.REJECTED.name
                it[reviewedBy] = org.jetbrains.exposed.dao.id.EntityID(adminUserId, com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable)
                it[updatedAt] = Instant.now()
            }
        }
    }

    override fun findRealPrice(productId: Int, sellerId: Int?): RealPriceRecord? =
        databaseFactory.withTransaction {
            PriceEntriesTable.selectAll()
                .where {
                    (PriceEntriesTable.product eq productId) and
                        if (sellerId == null) PriceEntriesTable.isAvailable eq true else PriceEntriesTable.seller eq sellerId
                }
                .map {
                    RealPriceRecord(
                        productPrice = it[PriceEntriesTable.price],
                        shippingFee = it[PriceEntriesTable.shippingFee],
                        totalPrice = it[PriceEntriesTable.totalPrice],
                        shippingType = it[PriceEntriesTable.shippingType].name,
                    )
                }
                .minByOrNull { it.totalPrice }
        }

    private fun toRecord(row: ResultRow): FraudAlertRecord =
        FraudAlertRecord(
            id = row[FraudAlertsTable.id].value,
            productId = row[FraudAlertsTable.product].value,
            sellerId = row[FraudAlertsTable.seller]?.value,
            priceEntryId = row[FraudAlertsTable.priceEntry]?.value,
            status = FraudAlertStatus.valueOf(row[FraudAlertsTable.status]),
            reason = row[FraudAlertsTable.reason],
            detectedPrice = row[FraudAlertsTable.detectedPrice],
            averagePrice = row[FraudAlertsTable.averagePrice],
            deviationPercent = row[FraudAlertsTable.deviationPercent].toDouble(),
            reviewedBy = row[FraudAlertsTable.reviewedBy]?.value,
            createdAt = row[FraudAlertsTable.createdAt],
            updatedAt = row[FraudAlertsTable.updatedAt],
        )
}
