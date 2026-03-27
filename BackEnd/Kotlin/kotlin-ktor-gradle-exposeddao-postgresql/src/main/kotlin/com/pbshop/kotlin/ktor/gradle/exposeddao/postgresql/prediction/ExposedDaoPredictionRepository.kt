package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceHistoryTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PricePredictionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDate

class ExposedDaoPredictionRepository(
    private val databaseFactory: DatabaseFactory,
) : PredictionRepository {
    override fun predictPriceTrend(
        productId: Int,
        days: Int,
    ): PriceTrendPredictionRecord? =
        databaseFactory.withTransaction {
            PricePredictionsTable.selectAll()
                .where {
                    (PricePredictionsTable.product eq productId) and
                        (PricePredictionsTable.predictionDate greaterEq LocalDate.now())
                }.orderBy(PricePredictionsTable.predictionDate to SortOrder.ASC)
                .limit(1)
                .firstOrNull()
                ?.let {
                    PriceTrendPredictionRecord(
                        productId = productId,
                        trend = inferTrend(productId),
                        recommendation = it[PricePredictionsTable.recommendation],
                        expectedLowestPrice = it[PricePredictionsTable.predictedPrice],
                        confidence = it[PricePredictionsTable.confidence].toDouble(),
                        updatedAt = it[PricePredictionsTable.createdAt],
                    )
                }
                ?: fallbackFromHistory(productId, days)
        }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.limit(1).any()
        }

    private fun inferTrend(productId: Int): String {
        val prices =
            PriceHistoryTable.selectAll()
                .where { PriceHistoryTable.product eq productId }
                .orderBy(PriceHistoryTable.date to SortOrder.ASC)
                .map { it[PriceHistoryTable.averagePrice] }
        if (prices.size < 2) return "STABLE"
        return when {
            prices.last() < prices.first() -> "FALLING"
            prices.last() > prices.first() -> "RISING"
            else -> "STABLE"
        }
    }

    private fun fallbackFromHistory(
        productId: Int,
        days: Int,
    ): PriceTrendPredictionRecord? {
        val rows =
            PriceHistoryTable.selectAll()
                .where { (PriceHistoryTable.product eq productId) and (PriceHistoryTable.date greaterEq LocalDate.now().minusDays(days.toLong())) }
                .orderBy(PriceHistoryTable.date to SortOrder.ASC)
                .toList()
        if (rows.isEmpty()) return null
        val first = rows.first()[PriceHistoryTable.averagePrice]
        val last = rows.last()[PriceHistoryTable.averagePrice]
        val trend =
            when {
                last < first -> "FALLING"
                last > first -> "RISING"
                else -> "STABLE"
            }
        return PriceTrendPredictionRecord(
            productId = productId,
            trend = trend,
            recommendation = if (trend == "FALLING") "BUY_SOON" else if (trend == "RISING") "WAIT" else "WATCH",
            expectedLowestPrice = rows.minOf { it[PriceHistoryTable.lowestPrice] },
            confidence = 0.55,
            updatedAt = Instant.now(),
        )
    }
}
