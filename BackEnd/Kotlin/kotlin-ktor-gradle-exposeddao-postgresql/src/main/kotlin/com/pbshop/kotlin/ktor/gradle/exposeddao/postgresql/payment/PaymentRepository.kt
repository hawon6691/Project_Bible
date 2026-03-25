package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentMethod
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentStatus
import java.time.Instant

data class PaymentRecord(
    val id: Int,
    val userId: Int,
    val orderId: Int,
    val orderNumber: String,
    val method: PaymentMethod,
    val amount: Int,
    val status: PaymentStatus,
    val paidAt: Instant?,
    val refundedAt: Instant?,
    val createdAt: Instant,
)

interface PaymentRepository {
    fun createPayment(
        userId: Int,
        orderId: Int,
        orderNumber: String,
        method: PaymentMethod,
        amount: Int,
    ): PaymentRecord

    fun findPaymentById(
        userId: Int,
        paymentId: Int,
    ): PaymentRecord?

    fun refundPayment(
        userId: Int,
        paymentId: Int,
    ): PaymentRecord
}
