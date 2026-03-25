package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import java.time.Instant
import java.time.LocalDate

data class PriceEntryRecord(
    val id: Int,
    val productId: Int,
    val productName: String?,
    val sellerId: Int,
    val sellerName: String,
    val sellerLogoUrl: String?,
    val trustScore: Int,
    val price: Int,
    val shippingCost: Int,
    val shippingInfo: String?,
    val productUrl: String,
    val shippingFee: Int,
    val shippingType: String,
    val totalPrice: Int,
    val clickCount: Int,
    val isAvailable: Boolean,
    val crawledAt: Instant?,
)

data class PriceHistoryRecord(
    val date: LocalDate,
    val lowestPrice: Int,
    val averagePrice: Int,
    val highestPrice: Int,
)

data class PriceAlertRecord(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String?,
    val targetPrice: Int,
    val isTriggered: Boolean,
    val triggeredAt: Instant?,
    val isActive: Boolean,
    val createdAt: Instant,
)

data class PriceAlertListResult(
    val items: List<PriceAlertRecord>,
    val totalCount: Int,
)

data class NewPriceEntry(
    val sellerId: Int,
    val price: Int,
    val shippingCost: Int,
    val shippingInfo: String?,
    val productUrl: String,
    val shippingFee: Int,
    val shippingType: String,
    val isAvailable: Boolean,
)

data class PriceEntryUpdate(
    val sellerId: Int?,
    val price: Int?,
    val shippingCost: Int?,
    val shippingInfo: String?,
    val productUrl: String?,
    val shippingFee: Int?,
    val shippingType: String?,
    val isAvailable: Boolean?,
)

data class NewPriceAlert(
    val productId: Int,
    val targetPrice: Int,
    val isActive: Boolean,
)

interface PriceRepository {
    fun productExists(productId: Int): Boolean

    fun sellerExists(sellerId: Int): Boolean

    fun listProductPrices(productId: Int): List<PriceEntryRecord>

    fun findPriceEntryById(id: Int): PriceEntryRecord?

    fun createPriceEntry(
        productId: Int,
        newEntry: NewPriceEntry,
    ): PriceEntryRecord

    fun updatePriceEntry(
        id: Int,
        update: PriceEntryUpdate,
    ): PriceEntryRecord

    fun deletePriceEntry(id: Int)

    fun listPriceHistory(productId: Int): List<PriceHistoryRecord>

    fun listPriceAlerts(
        userId: Int,
        page: Int,
        limit: Int,
    ): PriceAlertListResult

    fun createPriceAlert(
        userId: Int,
        newAlert: NewPriceAlert,
    ): PriceAlertRecord
}
