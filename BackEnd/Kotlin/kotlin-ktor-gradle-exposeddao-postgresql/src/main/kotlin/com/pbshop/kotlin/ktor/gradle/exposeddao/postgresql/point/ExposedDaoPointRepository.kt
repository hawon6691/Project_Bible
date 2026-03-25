package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PointTransactionEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PointTransactionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PointType
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UserEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.ZoneOffset

class ExposedDaoPointRepository(
    private val databaseFactory: DatabaseFactory,
) : PointRepository {
    override fun userExists(userId: Int): Boolean =
        databaseFactory.withTransaction {
            !UsersTable.selectAll().where { UsersTable.id eq userId }.limit(1).empty()
        }

    override fun getBalance(userId: Int): PointBalanceRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            val now = Instant.now()
            val expiring =
                PointTransactionsTable
                    .selectAll()
                    .where {
                        (PointTransactionsTable.user eq userId) and
                            (PointTransactionsTable.expiresAt.isNotNull()) and
                            (PointTransactionsTable.expiresAt greaterEq now)
                    }.map { it[PointTransactionsTable.amount] }
                    .sum()
            val expiringDate =
                PointTransactionsTable
                    .selectAll()
                    .where {
                        (PointTransactionsTable.user eq userId) and
                            (PointTransactionsTable.expiresAt.isNotNull()) and
                            (PointTransactionsTable.expiresAt greaterEq now)
                    }.orderBy(PointTransactionsTable.expiresAt to SortOrder.ASC)
                    .limit(1)
                    .firstOrNull()
                    ?.get(PointTransactionsTable.expiresAt)
                    ?.atZone(ZoneOffset.UTC)
                    ?.toLocalDate()
                    ?.toString()
            PointBalanceRecord(user.point, expiring, expiringDate)
        }

    override fun listTransactions(
        userId: Int,
        page: Int,
        limit: Int,
        type: PointType?,
    ): PointTransactionListResult =
        databaseFactory.withTransaction {
            val filtered =
                PointTransactionsTable
                    .selectAll()
                    .let { query ->
                        if (type == null) {
                            query.where { PointTransactionsTable.user eq userId }
                        } else {
                            query.where { (PointTransactionsTable.user eq userId) and (PointTransactionsTable.type eq type) }
                        }
                    }.orderBy(PointTransactionsTable.createdAt to SortOrder.DESC, PointTransactionsTable.id to SortOrder.DESC)
                    .map(::toRecord)
            val offset = (page - 1) * limit
            PointTransactionListResult(filtered.drop(offset).take(limit), filtered.size)
        }

    override fun createTransaction(
        userId: Int,
        transaction: NewPointTransaction,
    ): PointTransactionRecord =
        databaseFactory.withTransaction {
            val user = requireNotNull(UserEntity.findById(userId)) { "User $userId not found" }
            val nextBalance =
                when (transaction.type) {
                    PointType.USE, PointType.EXPIRE -> (user.point - transaction.amount).coerceAtLeast(0)
                    else -> user.point + transaction.amount
                }
            user.point = nextBalance
            user.updatedAt = Instant.now()
            val created =
                PointTransactionEntity.new {
                    this.userId = EntityID(userId, UsersTable)
                    type = transaction.type
                    amount = transaction.amount
                    balance = nextBalance
                    description = transaction.description
                    referenceType = transaction.referenceType
                    referenceId = transaction.referenceId
                    expiresAt = transaction.expiresAt
                    createdAt = Instant.now()
                }
            toRecord(
                PointTransactionsTable.selectAll()
                    .where { PointTransactionsTable.id eq created.id.value }
                    .single(),
            )
        }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): PointTransactionRecord =
        PointTransactionRecord(
            id = row[PointTransactionsTable.id].value,
            userId = row[PointTransactionsTable.user].value,
            type = row[PointTransactionsTable.type],
            amount = row[PointTransactionsTable.amount],
            balance = row[PointTransactionsTable.balance],
            description = row[PointTransactionsTable.description],
            referenceType = row[PointTransactionsTable.referenceType],
            referenceId = row[PointTransactionsTable.referenceId],
            expiresAt = row[PointTransactionsTable.expiresAt],
            createdAt = row[PointTransactionsTable.createdAt],
        )
}
