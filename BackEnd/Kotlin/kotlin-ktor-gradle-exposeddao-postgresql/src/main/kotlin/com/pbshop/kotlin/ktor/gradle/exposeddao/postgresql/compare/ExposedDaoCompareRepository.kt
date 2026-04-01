package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CompareItemsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductSpecsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SpecDefinitionsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoCompareRepository(
    private val databaseFactory: DatabaseFactory,
) : CompareRepository {
    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll()
                .where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }
                .empty()
        }

    override fun add(compareKey: String, productId: Int) {
        databaseFactory.withTransaction {
            CompareItemsTable.insertIgnore {
                it[CompareItemsTable.compareKey] = compareKey
                it[CompareItemsTable.product] = org.jetbrains.exposed.dao.id.EntityID(productId, ProductsTable)
                it[createdAt] = Instant.now()
                it[updatedAt] = Instant.now()
            }
        }
    }

    override fun remove(compareKey: String, productId: Int) {
        databaseFactory.withTransaction {
            CompareItemsTable.deleteWhere { (CompareItemsTable.compareKey eq compareKey) and (CompareItemsTable.product eq productId) }
        }
    }

    override fun list(compareKey: String): List<CompareItemRecord> =
        databaseFactory.withTransaction {
            CompareItemsTable.innerJoin(ProductsTable)
                .selectAll()
                .where { (CompareItemsTable.compareKey eq compareKey) and ProductsTable.deletedAt.isNull() }
                .map {
                    CompareItemRecord(
                        productId = it[ProductsTable.id].value,
                        name = it[ProductsTable.name],
                        slug = slugify(it[ProductsTable.name]),
                        thumbnailUrl = it[ProductsTable.thumbnailUrl],
                    )
                }
        }

    override fun detail(compareKey: String): List<CompareDetailItemRecord> =
        databaseFactory.withTransaction {
            val productRows =
                CompareItemsTable.innerJoin(ProductsTable)
                    .selectAll()
                    .where { (CompareItemsTable.compareKey eq compareKey) and ProductsTable.deletedAt.isNull() }
                    .associateBy { it[ProductsTable.id].value }

            val bestPrices =
                PriceEntriesTable.selectAll()
                    .where { PriceEntriesTable.product inList productRows.keys }
                    .groupBy { it[PriceEntriesTable.product].value }
                    .mapValues { (_, rows) -> rows.minOfOrNull { it[PriceEntriesTable.totalPrice] } ?: 0 }

            val specsByProduct =
                ProductSpecsTable.innerJoin(SpecDefinitionsTable)
                    .selectAll()
                    .where { ProductSpecsTable.product inList productRows.keys }
                    .groupBy { it[ProductSpecsTable.product].value }
                    .mapValues { (_, rows) ->
                        rows.associate { row ->
                            row[SpecDefinitionsTable.name] to row[ProductSpecsTable.value]
                        }
                    }

            productRows.keys.mapNotNull { productId ->
                productRows[productId]?.let { row ->
                    CompareDetailItemRecord(
                        productId = productId,
                        name = row[ProductsTable.name],
                        slug = slugify(row[ProductsTable.name]),
                        categoryId = row[ProductsTable.category].value,
                        bestPrice = bestPrices[productId] ?: row[ProductsTable.discountPrice] ?: row[ProductsTable.price],
                        ratingAvg = row[ProductsTable.averageRating].toDouble(),
                        specs = specsByProduct[productId].orEmpty(),
                    )
                }
            }
        }

    private fun slugify(name: String): String =
        name.lowercase()
            .replace(Regex("[^a-z0-9가-힣]+"), "-")
            .trim('-')
}
