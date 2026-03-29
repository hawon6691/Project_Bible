package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductMappingsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoMatchingRepository(
    private val databaseFactory: DatabaseFactory,
) : MatchingRepository {
    override fun listPending(page: Int, limit: Int): ProductMappingPageResult =
        databaseFactory.withTransaction {
            val rows =
                ProductMappingsTable.selectAll()
                    .where { ProductMappingsTable.status eq ProductMappingStatus.PENDING.name }
                    .orderBy(ProductMappingsTable.createdAt to SortOrder.DESC)
                    .map(::toRecord)
            val fromIndex = ((page - 1) * limit).coerceAtMost(rows.size)
            val toIndex = (fromIndex + limit).coerceAtMost(rows.size)
            ProductMappingPageResult(rows.subList(fromIndex, toIndex), rows.size)
        }

    override fun findById(id: Int): ProductMappingRecord? =
        databaseFactory.withTransaction {
            ProductMappingsTable.selectAll()
                .where { ProductMappingsTable.id eq id }
                .singleOrNull()
                ?.let(::toRecord)
        }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.empty()
        }

    override fun approve(id: Int, productId: Int, reviewedBy: Int): ProductMappingRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            ProductMappingsTable.update({ ProductMappingsTable.id eq id }) {
                it[ProductMappingsTable.productId] = productId
                it[ProductMappingsTable.status] = ProductMappingStatus.APPROVED.name
                it[ProductMappingsTable.reason] = null
                it[ProductMappingsTable.reviewedBy] = reviewedBy
                it[ProductMappingsTable.reviewedAt] = now
                it[ProductMappingsTable.updatedAt] = now
            }
            findById(id)!!
        }

    override fun reject(id: Int, reason: String, reviewedBy: Int): ProductMappingRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            ProductMappingsTable.update({ ProductMappingsTable.id eq id }) {
                it[ProductMappingsTable.status] = ProductMappingStatus.REJECTED.name
                it[ProductMappingsTable.reason] = reason
                it[ProductMappingsTable.reviewedBy] = reviewedBy
                it[ProductMappingsTable.reviewedAt] = now
                it[ProductMappingsTable.updatedAt] = now
            }
            findById(id)!!
        }

    override fun autoMatch(reviewedBy: Int): Map<String, Int> =
        databaseFactory.withTransaction {
            val candidates =
                ProductsTable.selectAll()
                    .where { ProductsTable.deletedAt.isNull() }
                    .map { it[ProductsTable.id].value to it[ProductsTable.name] }
            var matchedCount = 0
            ProductMappingsTable.selectAll()
                .where { ProductMappingsTable.status eq ProductMappingStatus.PENDING.name }
                .orderBy(ProductMappingsTable.createdAt to SortOrder.ASC)
                .map(::toRecord)
                .forEach { mapping ->
                    val keyword = mapping.sourceName.substringBefore(" ").trim()
                    val candidate = candidates.firstOrNull { (_, name) -> keyword.isNotBlank() && name.contains(keyword, ignoreCase = true) }
                    if (candidate != null) {
                        matchedCount += 1
                        approve(mapping.id, candidate.first, reviewedBy)
                    }
                }
            val stats = stats()
            mapOf("matchedCount" to matchedCount, "pendingCount" to stats.pending)
        }

    override fun stats(): ProductMappingStats =
        databaseFactory.withTransaction {
            val statuses = ProductMappingsTable.selectAll().map { it[ProductMappingsTable.status] }
            val pending = statuses.count { it == ProductMappingStatus.PENDING.name }
            val approved = statuses.count { it == ProductMappingStatus.APPROVED.name }
            val rejected = statuses.count { it == ProductMappingStatus.REJECTED.name }
            ProductMappingStats(pending, approved, rejected, statuses.size)
        }

    private fun toRecord(row: ResultRow): ProductMappingRecord =
        ProductMappingRecord(
            id = row[ProductMappingsTable.id].value,
            sourceName = row[ProductMappingsTable.sourceName],
            sourceBrand = row[ProductMappingsTable.sourceBrand],
            sourceSeller = row[ProductMappingsTable.sourceSeller],
            sourceUrl = row[ProductMappingsTable.sourceUrl],
            productId = row[ProductMappingsTable.productId],
            status = ProductMappingStatus.valueOf(row[ProductMappingsTable.status]),
            confidence = row[ProductMappingsTable.confidence],
            reason = row[ProductMappingsTable.reason],
            reviewedBy = row[ProductMappingsTable.reviewedBy],
            reviewedAt = row[ProductMappingsTable.reviewedAt],
            createdAt = row[ProductMappingsTable.createdAt],
            updatedAt = row[ProductMappingsTable.updatedAt],
        )
}
