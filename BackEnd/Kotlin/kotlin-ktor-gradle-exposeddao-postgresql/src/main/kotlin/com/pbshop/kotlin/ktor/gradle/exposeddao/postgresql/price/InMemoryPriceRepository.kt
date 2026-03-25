package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import java.time.Instant
import java.time.LocalDate

class InMemoryPriceRepository(
    seededEntries: List<PriceEntryRecord> = emptyList(),
    seededHistory: Map<Int, List<PriceHistoryRecord>> = emptyMap(),
    seededAlerts: List<PriceAlertRecord> = emptyList(),
    seededProducts: Map<Int, String> = emptyMap(),
    seededSellers: Map<Int, Triple<String, String?, Int>> = emptyMap(),
) : PriceRepository {
    private val entries = linkedMapOf<Int, PriceEntryRecord>()
    private val history = seededHistory.mapValues { it.value.toMutableList() }.toMutableMap()
    private val alerts = linkedMapOf<Int, PriceAlertRecord>()
    private val products = seededProducts.toMutableMap()
    private val sellers = seededSellers.toMutableMap()
    private var nextEntryId = 1
    private var nextAlertId = 1

    init {
        seededEntries.forEach {
            entries[it.id] = it
            nextEntryId = maxOf(nextEntryId, it.id + 1)
        }
        seededAlerts.forEach {
            alerts[it.id] = it
            nextAlertId = maxOf(nextAlertId, it.id + 1)
        }
    }

    override fun productExists(productId: Int): Boolean = products.containsKey(productId)

    override fun sellerExists(sellerId: Int): Boolean = sellers.containsKey(sellerId)

    override fun listProductPrices(productId: Int): List<PriceEntryRecord> =
        entries.values.filter { it.productId == productId }.sortedBy { it.price }

    override fun findPriceEntryById(id: Int): PriceEntryRecord? = entries[id]

    override fun createPriceEntry(
        productId: Int,
        newEntry: NewPriceEntry,
    ): PriceEntryRecord {
        val seller = requireNotNull(sellers[newEntry.sellerId]) { "Seller ${newEntry.sellerId} not found" }
        val created =
            PriceEntryRecord(
                id = nextEntryId++,
                productId = productId,
                productName = products[productId],
                sellerId = newEntry.sellerId,
                sellerName = seller.first,
                sellerLogoUrl = seller.second,
                trustScore = seller.third,
                price = newEntry.price,
                shippingCost = newEntry.shippingCost,
                shippingInfo = newEntry.shippingInfo,
                productUrl = newEntry.productUrl,
                shippingFee = newEntry.shippingFee,
                shippingType = newEntry.shippingType,
                totalPrice = newEntry.price + newEntry.shippingFee,
                clickCount = 0,
                isAvailable = newEntry.isAvailable,
                crawledAt = Instant.now(),
            )
        entries[created.id] = created
        return created
    }

    override fun updatePriceEntry(
        id: Int,
        update: PriceEntryUpdate,
    ): PriceEntryRecord {
        val current = requireNotNull(entries[id]) { "Price entry $id not found" }
        val sellerId = update.sellerId ?: current.sellerId
        val seller = requireNotNull(sellers[sellerId]) { "Seller $sellerId not found" }
        val updated =
            current.copy(
                sellerId = sellerId,
                sellerName = seller.first,
                sellerLogoUrl = seller.second,
                trustScore = seller.third,
                price = update.price ?: current.price,
                shippingCost = update.shippingCost ?: current.shippingCost,
                shippingInfo = update.shippingInfo ?: current.shippingInfo,
                productUrl = update.productUrl ?: current.productUrl,
                shippingFee = update.shippingFee ?: current.shippingFee,
                shippingType = update.shippingType ?: current.shippingType,
                totalPrice = (update.price ?: current.price) + (update.shippingFee ?: current.shippingFee),
                isAvailable = update.isAvailable ?: current.isAvailable,
                crawledAt = Instant.now(),
            )
        entries[id] = updated
        return updated
    }

    override fun deletePriceEntry(id: Int) {
        entries.remove(id) ?: error("Price entry $id not found")
    }

    override fun listPriceHistory(productId: Int): List<PriceHistoryRecord> =
        history[productId].orEmpty().sortedByDescending { it.date }

    override fun listPriceAlerts(
        userId: Int,
        page: Int,
        limit: Int,
    ): PriceAlertListResult {
        val filtered = alerts.values.filter { it.userId == userId }.sortedByDescending { it.id }
        val offset = (page - 1) * limit
        return PriceAlertListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun createPriceAlert(
        userId: Int,
        newAlert: NewPriceAlert,
    ): PriceAlertRecord {
        val created =
            PriceAlertRecord(
                id = nextAlertId++,
                userId = userId,
                productId = newAlert.productId,
                productName = products[newAlert.productId],
                targetPrice = newAlert.targetPrice,
                isTriggered = false,
                triggeredAt = null,
                isActive = newAlert.isActive,
                createdAt = Instant.now(),
            )
        alerts[created.id] = created
        return created
    }

    companion object {
        fun seeded(): InMemoryPriceRepository =
            InMemoryPriceRepository(
                seededEntries =
                    listOf(
                        PriceEntryRecord(1, 1, "게이밍 노트북 A15", 1, "공식몰", "https://img.example.com/s1-logo.png", 92, 1720000, 0, "무료배송", "https://official.example.com/p/1", 0, "FREE", 1720000, 120, true, Instant.now()),
                        PriceEntryRecord(2, 1, "게이밍 노트북 A15", 2, "테크마켓", "https://img.example.com/s2-logo.png", 84, 1750000, 3000, "기본배송", "https://techmarket.example.com/p/1", 3000, "PAID", 1753000, 98, true, Instant.now()),
                        PriceEntryRecord(3, 2, "사무용 노트북 Slim", 1, "공식몰", "https://img.example.com/s1-logo.png", 92, 940000, 0, "무료배송", "https://official.example.com/p/2", 0, "FREE", 940000, 67, true, Instant.now()),
                    ),
                seededHistory =
                    mapOf(
                        1 to listOf(PriceHistoryRecord(LocalDate.now().minusDays(2), 1760000, 1785000, 1810000), PriceHistoryRecord(LocalDate.now().minusDays(1), 1740000, 1760000, 1790000), PriceHistoryRecord(LocalDate.now(), 1720000, 1735000, 1753000)),
                        2 to listOf(PriceHistoryRecord(LocalDate.now().minusDays(1), 950000, 960000, 975000), PriceHistoryRecord(LocalDate.now(), 940000, 947500, 955000)),
                    ),
                seededAlerts =
                    listOf(
                        PriceAlertRecord(1, 4, 1, "게이밍 노트북 A15", 1700000, false, null, true, Instant.now().minusSeconds(86400)),
                        PriceAlertRecord(2, 5, 2, "사무용 노트북 Slim", 930000, false, null, true, Instant.now().minusSeconds(72000)),
                    ),
                seededProducts = mapOf(1 to "게이밍 노트북 A15", 2 to "사무용 노트북 Slim", 3 to "미니 데스크탑 Pro", 4 to "전기차 EV 520"),
                seededSellers =
                    mapOf(
                        1 to Triple("공식몰", "https://img.example.com/s1-logo.png", 92),
                        2 to Triple("테크마켓", "https://img.example.com/s2-logo.png", 84),
                        3 to Triple("카월드", "https://img.example.com/s3-logo.png", 79),
                    ),
            )
    }
}
