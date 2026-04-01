package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceHistoryTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductSpecsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SpecDefinitionsTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate

class ExposedDaoAnalyticsRepository(
    private val databaseFactory: DatabaseFactory,
) : AnalyticsRepository {
    override fun findLowestEver(productId: Int): LowestEverRecord? =
        databaseFactory.withTransaction {
            val currentPrice =
                PriceEntriesTable.selectAll()
                    .where { (PriceEntriesTable.product eq productId) and (PriceEntriesTable.isAvailable eq true) }
                    .minOfOrNull { it[PriceEntriesTable.totalPrice] }
                    ?: ProductsTable.selectAll().where { ProductsTable.id eq productId }.singleOrNull()?.let { row ->
                        row[ProductsTable.discountPrice] ?: row[ProductsTable.price]
                    }
                    ?: return@withTransaction null

            val lowestRow =
                PriceHistoryTable.selectAll()
                    .where { PriceHistoryTable.product eq productId }
                    .orderBy(PriceHistoryTable.lowestPrice to SortOrder.ASC, PriceHistoryTable.date to SortOrder.ASC)
                    .firstOrNull()

            LowestEverRecord(
                currentPrice = currentPrice,
                lowestPrice = lowestRow?.get(PriceHistoryTable.lowestPrice) ?: currentPrice,
                lowestDate = lowestRow?.get(PriceHistoryTable.date) ?: LocalDate.now(),
            )
        }

    override fun findUnitPrice(productId: Int): UnitPriceRecord? =
        databaseFactory.withTransaction {
            val currentPrice =
                PriceEntriesTable.selectAll()
                    .where { (PriceEntriesTable.product eq productId) and (PriceEntriesTable.isAvailable eq true) }
                    .minOfOrNull { it[PriceEntriesTable.totalPrice] }
                    ?: ProductsTable.selectAll().where { ProductsTable.id eq productId }.singleOrNull()?.let { row ->
                        row[ProductsTable.discountPrice] ?: row[ProductsTable.price]
                    }
                    ?: return@withTransaction null

            val specRow =
                ProductSpecsTable.innerJoin(SpecDefinitionsTable)
                    .selectAll()
                    .where { ProductSpecsTable.product eq productId }
                    .firstOrNull { row ->
                        val name = row[SpecDefinitionsTable.name]
                        name.contains("용량") || name.contains("수량") || name.equals("RAM", ignoreCase = true)
                    }
            val rawValue = specRow?.get(ProductSpecsTable.value).orEmpty()
            val quantity = Regex("""\d+(\.\d+)?""").find(rawValue)?.value?.toDoubleOrNull() ?: 1.0
            val unit = rawValue.replace(Regex("""\d+(\.\d+)?"""), "").trim().ifBlank { "ea" }
            UnitPriceRecord(
                unitPrice = currentPrice / quantity,
                unit = unit,
                quantity = quantity,
            )
        }
}
