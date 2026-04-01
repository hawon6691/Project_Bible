package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PcBuildPartsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PcBuildsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsedPricesTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll

class ExposedDaoUsedMarketRepository(
    private val databaseFactory: DatabaseFactory,
) : UsedMarketRepository {
    override fun productPrice(productId: Int): UsedPriceSummaryRecord? =
        databaseFactory.withTransaction {
            val rows =
                UsedPricesTable.innerJoin(ProductsTable)
                    .selectAll()
                    .where { UsedPricesTable.product eq productId }
                    .orderBy(UsedPricesTable.collectedAt to SortOrder.ASC)
                    .map {
                        UsedPriceSummaryRecord(
                            productId = it[ProductsTable.id].value,
                            productName = it[ProductsTable.name],
                            averagePrice = it[UsedPricesTable.averagePrice],
                            minPrice = it[UsedPricesTable.minPrice],
                            maxPrice = it[UsedPricesTable.maxPrice],
                            sampleCount = it[UsedPricesTable.sampleCount],
                            trend = "STABLE",
                        )
                    }
            if (rows.isEmpty()) return@withTransaction null
            val latest = rows.last()
            val previous = rows.getOrNull(rows.lastIndex - 1)
            latest.copy(trend = trend(previous?.averagePrice, latest.averagePrice))
        }

    override fun categoryPrices(categoryId: Int, page: Int, limit: Int): UsedPricePageResult =
        databaseFactory.withTransaction {
            val grouped =
                UsedPricesTable.innerJoin(ProductsTable)
                    .innerJoin(CategoriesTable)
                    .selectAll()
                    .where { ProductsTable.category eq categoryId }
                    .groupBy { it[ProductsTable.id].value }
                    .mapNotNull { (_, rows) ->
                        val sorted = rows.sortedBy { it[UsedPricesTable.collectedAt] }
                        val latest = sorted.lastOrNull() ?: return@mapNotNull null
                        val previous = sorted.getOrNull(sorted.lastIndex - 1)
                        UsedPriceSummaryRecord(
                            productId = latest[ProductsTable.id].value,
                            productName = latest[ProductsTable.name],
                            averagePrice = latest[UsedPricesTable.averagePrice],
                            minPrice = latest[UsedPricesTable.minPrice],
                            maxPrice = latest[UsedPricesTable.maxPrice],
                            sampleCount = latest[UsedPricesTable.sampleCount],
                            trend = trend(previous?.get(UsedPricesTable.averagePrice), latest[UsedPricesTable.averagePrice]),
                        )
                    }
                    .sortedBy { it.productId }
            val from = ((page - 1) * limit).coerceAtMost(grouped.size)
            val to = (from + limit).coerceAtMost(grouped.size)
            UsedPricePageResult(grouped.subList(from, to), grouped.size)
        }

    override fun estimateBuild(userId: Int, buildId: Int): UsedBuildEstimateRecord? =
        databaseFactory.withTransaction {
            val buildExists =
                !PcBuildsTable.selectAll()
                    .where { (PcBuildsTable.id eq buildId) and (PcBuildsTable.user eq userId) and PcBuildsTable.deletedAt.isNull() }
                    .empty()
            if (!buildExists) return@withTransaction null
            val parts =
                PcBuildPartsTable.innerJoin(ProductsTable)
                    .selectAll()
                    .where { PcBuildPartsTable.build eq buildId }
                    .map { row ->
                        val latest =
                            UsedPricesTable.selectAll()
                                .where { UsedPricesTable.product eq row[ProductsTable.id].value }
                                .orderBy(UsedPricesTable.collectedAt to SortOrder.DESC)
                                .firstOrNull()
                        val estimated = (latest?.get(UsedPricesTable.minPrice) ?: row[PcBuildPartsTable.unitPrice]) * row[PcBuildPartsTable.quantity]
                        UsedBuildEstimatePartRecord(
                            partType = row[PcBuildPartsTable.partType],
                            productId = row[ProductsTable.id].value,
                            productName = row[ProductsTable.name],
                            quantity = row[PcBuildPartsTable.quantity],
                            estimatedPrice = estimated,
                        )
                    }
            UsedBuildEstimateRecord(buildId, parts.sumOf { it.estimatedPrice }, parts)
        }

    private fun trend(previous: Int?, latest: Int): String =
        when {
            previous == null -> "STABLE"
            latest > previous -> "UP"
            latest < previous -> "DOWN"
            else -> "STABLE"
        }
}
