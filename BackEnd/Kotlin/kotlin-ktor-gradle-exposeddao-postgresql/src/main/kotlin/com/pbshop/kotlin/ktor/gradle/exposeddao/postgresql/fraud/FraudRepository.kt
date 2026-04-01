package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import java.time.Instant

enum class FraudAlertStatus {
    PENDING,
    APPROVED,
    REJECTED,
}

data class FraudAlertRecord(
    val id: Int,
    val productId: Int,
    val sellerId: Int?,
    val priceEntryId: Int?,
    val status: FraudAlertStatus,
    val reason: String,
    val detectedPrice: Int,
    val averagePrice: Int,
    val deviationPercent: Double,
    val reviewedBy: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class FraudAlertPageResult(
    val items: List<FraudAlertRecord>,
    val totalCount: Int,
)

data class RealPriceRecord(
    val productPrice: Int,
    val shippingFee: Int,
    val totalPrice: Int,
    val shippingType: String,
)

interface FraudRepository {
    fun listAlerts(status: FraudAlertStatus?, page: Int, limit: Int): FraudAlertPageResult

    fun findAlertById(id: Int): FraudAlertRecord?

    fun approveAlert(id: Int, adminUserId: Int)

    fun rejectAlert(id: Int, adminUserId: Int)

    fun findRealPrice(productId: Int, sellerId: Int?): RealPriceRecord?
}
