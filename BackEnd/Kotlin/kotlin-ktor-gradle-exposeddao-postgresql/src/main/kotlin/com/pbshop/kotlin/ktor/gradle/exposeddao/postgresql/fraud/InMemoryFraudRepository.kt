package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import java.time.Instant

class InMemoryFraudRepository(
    alerts: List<FraudAlertRecord>,
    private val realPrices: List<Pair<Pair<Int, Int?>, RealPriceRecord>>,
) : FraudRepository {
    private val alerts = linkedMapOf<Int, FraudAlertRecord>()

    init {
        alerts.forEach { this.alerts[it.id] = it }
    }

    override fun listAlerts(status: FraudAlertStatus?, page: Int, limit: Int): FraudAlertPageResult {
        val filtered = alerts.values.filter { status == null || it.status == status }.sortedByDescending { it.createdAt }
        val from = ((page - 1) * limit).coerceAtMost(filtered.size)
        val to = (from + limit).coerceAtMost(filtered.size)
        return FraudAlertPageResult(filtered.subList(from, to), filtered.size)
    }

    override fun findAlertById(id: Int): FraudAlertRecord? = alerts[id]

    override fun approveAlert(id: Int, adminUserId: Int) {
        val current = alerts[id] ?: error("Alert $id not found")
        alerts[id] = current.copy(status = FraudAlertStatus.APPROVED, reviewedBy = adminUserId, updatedAt = Instant.now())
    }

    override fun rejectAlert(id: Int, adminUserId: Int) {
        val current = alerts[id] ?: error("Alert $id not found")
        alerts[id] = current.copy(status = FraudAlertStatus.REJECTED, reviewedBy = adminUserId, updatedAt = Instant.now())
    }

    override fun findRealPrice(productId: Int, sellerId: Int?): RealPriceRecord? =
        realPrices.firstOrNull { it.first.first == productId && it.first.second == sellerId }?.second
            ?: realPrices.firstOrNull { it.first.first == productId && it.first.second == null }?.second

    companion object {
        fun seeded(): InMemoryFraudRepository {
            val now = Instant.parse("2026-03-20T12:00:00Z")
            return InMemoryFraudRepository(
                alerts =
                    listOf(
                        FraudAlertRecord(1, 1, 1, 1, FraudAlertStatus.PENDING, "평균 가격 대비 급격히 낮은 가격이 감지되었습니다.", 1290000, 1790000, 27.9, null, now, now),
                        FraudAlertRecord(2, 2, 2, 2, FraudAlertStatus.APPROVED, "이상 가격 승인", 890000, 980000, 9.1, 1, now.minusSeconds(5000), now.minusSeconds(1000)),
                    ),
                realPrices =
                    listOf(
                        (1 to null) to RealPriceRecord(1690000, 3000, 1693000, "PAID"),
                        (1 to 1) to RealPriceRecord(1690000, 3000, 1693000, "PAID"),
                        (2 to null) to RealPriceRecord(940000, 0, 940000, "FREE"),
                    ),
            )
        }
    }
}
