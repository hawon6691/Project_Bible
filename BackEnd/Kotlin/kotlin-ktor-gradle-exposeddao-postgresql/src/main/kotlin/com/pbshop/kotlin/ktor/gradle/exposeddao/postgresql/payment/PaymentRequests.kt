package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import kotlinx.serialization.Serializable

@Serializable
data class PaymentCreateRequest(
    val orderId: Int,
    val method: String,
    val amount: Int? = null,
)
