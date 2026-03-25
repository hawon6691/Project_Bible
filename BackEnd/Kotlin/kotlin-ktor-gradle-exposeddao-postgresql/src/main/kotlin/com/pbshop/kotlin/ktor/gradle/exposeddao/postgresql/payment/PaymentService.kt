package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PaymentMethod
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order.OrderRepository
import io.ktor.http.HttpStatusCode

class PaymentService(
    private val repository: PaymentRepository,
    private val orderRepository: OrderRepository,
) {
    fun create(
        userId: Int,
        request: PaymentCreateRequest,
    ): StubResponse {
        val order =
            orderRepository.findOrderDetailById(userId, request.orderId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다.")
        val amount = request.amount ?: order.finalAmount
        if (amount <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "결제 금액은 0보다 커야 합니다.")
        }
        val created =
            repository.createPayment(
                userId = userId,
                orderId = order.id,
                orderNumber = order.orderNumber,
                method = parseMethod(request.method),
                amount = amount,
            )
        return StubResponse(status = HttpStatusCode.Created, data = payload(created))
    }

    fun detail(
        userId: Int,
        paymentId: Int,
    ): StubResponse =
        StubResponse(
            data =
                payload(
                    repository.findPaymentById(userId, paymentId)
                        ?: throw PbShopException(HttpStatusCode.NotFound, "PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."),
                ),
        )

    fun refund(
        userId: Int,
        paymentId: Int,
    ): StubResponse =
        StubResponse(
            data =
                payload(
                    repository.refundPayment(userId, paymentId),
                ),
        )

    private fun parseMethod(value: String): PaymentMethod =
        runCatching { PaymentMethod.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 결제 수단입니다.")
            }

    private fun payload(record: PaymentRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "orderId" to record.orderId,
            "orderNumber" to record.orderNumber,
            "method" to record.method.name,
            "amount" to record.amount,
            "status" to record.status.name,
            "provider" to "PBPAY",
            "paidAt" to record.paidAt?.toString(),
            "refundedAt" to record.refundedAt?.toString(),
            "createdAt" to record.createdAt.toString(),
        )
}
