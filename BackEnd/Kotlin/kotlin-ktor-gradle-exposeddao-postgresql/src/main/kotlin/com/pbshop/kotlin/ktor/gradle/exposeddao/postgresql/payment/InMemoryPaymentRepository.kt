package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentMethod
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentStatus
import java.time.Instant

class InMemoryPaymentRepository(
    seededPayments: List<PaymentRecord> = emptyList(),
) : PaymentRepository {
    private val payments = linkedMapOf<Int, PaymentRecord>()
    private var nextId = 1

    init {
        seededPayments.forEach {
            payments[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun createPayment(
        userId: Int,
        orderId: Int,
        orderNumber: String,
        method: PaymentMethod,
        amount: Int,
    ): PaymentRecord {
        val now = Instant.now()
        val created =
            PaymentRecord(
                id = nextId++,
                userId = userId,
                orderId = orderId,
                orderNumber = orderNumber,
                method = method,
                amount = amount,
                status = PaymentStatus.COMPLETED,
                paidAt = now,
                refundedAt = null,
                createdAt = now,
            )
        payments[created.id] = created
        return created
    }

    override fun findPaymentById(
        userId: Int,
        paymentId: Int,
    ): PaymentRecord? = payments[paymentId]?.takeIf { it.userId == userId }

    override fun refundPayment(
        userId: Int,
        paymentId: Int,
    ): PaymentRecord {
        val current = requireNotNull(findPaymentById(userId, paymentId)) { "Payment $paymentId not found" }
        val updated =
            current.copy(
                status = PaymentStatus.REFUNDED,
                refundedAt = Instant.now(),
            )
        payments[paymentId] = updated
        return updated
    }

    companion object {
        fun seeded(): InMemoryPaymentRepository =
            InMemoryPaymentRepository(
                seededPayments =
                    listOf(
                        PaymentRecord(1, 4, 1, "ORD-20260225-A001", PaymentMethod.CARD, 2640000, PaymentStatus.COMPLETED, Instant.now().minusSeconds(86400), null, Instant.now().minusSeconds(86400)),
                        PaymentRecord(2, 5, 2, "ORD-20260225-A002", PaymentMethod.BANK_TRANSFER, 1360000, PaymentStatus.COMPLETED, Instant.now().minusSeconds(10800), null, Instant.now().minusSeconds(10800)),
                    ),
            )
    }
}
