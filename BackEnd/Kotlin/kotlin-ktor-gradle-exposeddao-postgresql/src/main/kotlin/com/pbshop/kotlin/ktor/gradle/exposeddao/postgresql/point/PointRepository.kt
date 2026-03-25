package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PointType
import java.time.Instant

data class PointBalanceRecord(
    val balance: Int,
    val expiringSoon: Int,
    val expiringDate: String?,
)

data class PointTransactionRecord(
    val id: Int,
    val userId: Int,
    val type: PointType,
    val amount: Int,
    val balance: Int,
    val description: String,
    val referenceType: String?,
    val referenceId: Int?,
    val expiresAt: Instant?,
    val createdAt: Instant,
)

data class PointTransactionListResult(
    val items: List<PointTransactionRecord>,
    val totalCount: Int,
)

data class NewPointTransaction(
    val type: PointType,
    val amount: Int,
    val description: String,
    val referenceType: String? = null,
    val referenceId: Int? = null,
    val expiresAt: Instant? = null,
)

interface PointRepository {
    fun userExists(userId: Int): Boolean

    fun getBalance(userId: Int): PointBalanceRecord

    fun listTransactions(
        userId: Int,
        page: Int,
        limit: Int,
        type: PointType? = null,
    ): PointTransactionListResult

    fun createTransaction(
        userId: Int,
        transaction: NewPointTransaction,
    ): PointTransactionRecord
}
