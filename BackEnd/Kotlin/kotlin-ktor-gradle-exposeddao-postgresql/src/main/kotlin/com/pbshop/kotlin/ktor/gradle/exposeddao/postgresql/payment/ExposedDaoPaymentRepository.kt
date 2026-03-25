package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrdersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentMethod
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoPaymentRepository(
    private val databaseFactory: DatabaseFactory,
) : PaymentRepository {
    override fun createPayment(
        userId: Int,
        orderId: Int,
        orderNumber: String,
        method: PaymentMethod,
        amount: Int,
    ): PaymentRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val created =
                PaymentEntity.new {
                    this.orderId = EntityID(orderId, OrdersTable)
                    this.method = method
                    this.amount = amount
                    status = PaymentStatus.COMPLETED
                    paidAt = now
                    refundedAt = null
                    createdAt = now
                    updatedAt = now
                }
            OrderEntity.findById(orderId)?.apply {
                status = OrderStatus.PAYMENT_CONFIRMED
                updatedAt = now
            }
            requireNotNull(findPaymentById(userId, created.id.value))
        }

    override fun findPaymentById(
        userId: Int,
        paymentId: Int,
    ): PaymentRecord? =
        databaseFactory.withTransaction {
            PaymentsTable
                .innerJoin(OrdersTable)
                .selectAll()
                .where { (PaymentsTable.id eq paymentId) and (OrdersTable.user eq userId) }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun refundPayment(
        userId: Int,
        paymentId: Int,
    ): PaymentRecord =
        databaseFactory.withTransaction {
            val entity =
                PaymentEntity.findById(paymentId)
                    ?.takeIf { payment ->
                        OrdersTable.selectAll()
                            .where { (OrdersTable.id eq payment.orderId.value) and (OrdersTable.user eq userId) }
                            .limit(1)
                            .count() > 0
                    } ?: error("Payment $paymentId not found")
            val now = Instant.now()
            entity.status = PaymentStatus.REFUNDED
            entity.refundedAt = now
            entity.updatedAt = now
            OrderEntity.findById(entity.orderId.value)?.apply {
                status = OrderStatus.CANCELLED
                updatedAt = now
            }
            requireNotNull(findPaymentById(userId, paymentId))
        }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): PaymentRecord =
        PaymentRecord(
            id = row[PaymentsTable.id].value,
            userId = row[OrdersTable.user].value,
            orderId = row[PaymentsTable.order].value,
            orderNumber = row[OrdersTable.orderNumber],
            method = row[PaymentsTable.method],
            amount = row[PaymentsTable.amount],
            status = row[PaymentsTable.status],
            paidAt = row[PaymentsTable.paidAt],
            refundedAt = row[PaymentsTable.refundedAt],
            createdAt = row[PaymentsTable.createdAt],
        )
}
