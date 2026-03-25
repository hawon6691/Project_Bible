package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PointType
import java.time.Instant
import java.time.LocalDate

class InMemoryPointRepository(
    seededTransactions: List<PointTransactionRecord> = emptyList(),
    seededBalances: Map<Int, PointBalanceRecord> = emptyMap(),
    private val userIds: Set<Int> = emptySet(),
) : PointRepository {
    private val transactions = linkedMapOf<Int, PointTransactionRecord>()
    private val balances = seededBalances.toMutableMap()
    private var nextId = 1

    init {
        seededTransactions.forEach {
            transactions[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun userExists(userId: Int): Boolean = userIds.contains(userId)

    override fun getBalance(userId: Int): PointBalanceRecord =
        balances[userId]
            ?: PointBalanceRecord(
                balance = transactions.values.filter { it.userId == userId }.maxByOrNull { it.createdAt }?.balance ?: 0,
                expiringSoon = 0,
                expiringDate = null,
            )

    override fun listTransactions(
        userId: Int,
        page: Int,
        limit: Int,
        type: PointType?,
    ): PointTransactionListResult {
        val filtered =
            transactions.values
                .filter { it.userId == userId && (type == null || it.type == type) }
                .sortedByDescending { it.id }
        val offset = (page - 1) * limit
        return PointTransactionListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun createTransaction(
        userId: Int,
        transaction: NewPointTransaction,
    ): PointTransactionRecord {
        val current = getBalance(userId)
        val nextBalance =
            when (transaction.type) {
                PointType.USE, PointType.EXPIRE -> (current.balance - transaction.amount).coerceAtLeast(0)
                else -> current.balance + transaction.amount
            }
        val created =
            PointTransactionRecord(
                id = nextId++,
                userId = userId,
                type = transaction.type,
                amount = transaction.amount,
                balance = nextBalance,
                description = transaction.description,
                referenceType = transaction.referenceType,
                referenceId = transaction.referenceId,
                expiresAt = transaction.expiresAt,
                createdAt = Instant.now(),
            )
        transactions[created.id] = created
        balances[userId] =
            PointBalanceRecord(
                balance = nextBalance,
                expiringSoon = if (transaction.expiresAt != null) transaction.amount else current.expiringSoon,
                expiringDate = transaction.expiresAt?.toString() ?: current.expiringDate,
            )
        return created
    }

    companion object {
        fun seeded(): InMemoryPointRepository =
            InMemoryPointRepository(
                seededTransactions =
                    listOf(
                        PointTransactionRecord(1, 4, PointType.EARN, 5000, 5000, "리뷰 작성 적립", "REVIEW", 1, null, Instant.now().minusSeconds(86400)),
                        PointTransactionRecord(2, 4, PointType.USE, 20000, 53000, "주문 시 포인트 사용", "ORDER", 1, null, Instant.now().minusSeconds(72000)),
                        PointTransactionRecord(3, 5, PointType.ADMIN_GRANT, 27000, 27000, "운영자 지급", "ADMIN", 1, null, Instant.now().minusSeconds(36000)),
                    ),
                seededBalances =
                    mapOf(
                        4 to PointBalanceRecord(53000, 3000, LocalDate.now().plusDays(7).toString()),
                        5 to PointBalanceRecord(27000, 0, null),
                    ),
                userIds = setOf(1, 4, 5),
            )
    }
}
