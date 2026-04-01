package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import java.time.LocalDate

class InMemoryAnalyticsRepository(
    private val lowestEver: Map<Int, LowestEverRecord>,
    private val unitPrices: Map<Int, UnitPriceRecord>,
) : AnalyticsRepository {
    override fun findLowestEver(productId: Int): LowestEverRecord? = lowestEver[productId]

    override fun findUnitPrice(productId: Int): UnitPriceRecord? = unitPrices[productId]

    companion object {
        fun seeded(): InMemoryAnalyticsRepository =
            InMemoryAnalyticsRepository(
                lowestEver =
                    mapOf(
                        1 to LowestEverRecord(currentPrice = 1720000, lowestPrice = 1680000, lowestDate = LocalDate.parse("2026-02-10")),
                        2 to LowestEverRecord(currentPrice = 940000, lowestPrice = 940000, lowestDate = LocalDate.parse("2026-03-01")),
                    ),
                unitPrices =
                    mapOf(
                        1 to UnitPriceRecord(unitPrice = 1720000.0, unit = "ea", quantity = 1.0),
                        2 to UnitPriceRecord(unitPrice = 940000.0, unit = "ea", quantity = 1.0),
                    ),
            )
    }
}
