package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecommendationType
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecommendationsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecentProductViewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.WishlistsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDate

class ExposedDaoRecommendationRepository(
    private val databaseFactory: DatabaseFactory,
) : RecommendationRepository {
    override fun listTodayRecommendations(limit: Int): List<RecommendationRecord> =
        databaseFactory.withTransaction {
            val today = LocalDate.now()
            RecommendationsTable.innerJoin(ProductsTable)
                .selectAll()
                .where {
                    (RecommendationsTable.type eq RecommendationType.TODAY) and
                        ProductsTable.deletedAt.isNull() and
                        ((RecommendationsTable.startDate.isNull()) or (RecommendationsTable.startDate lessEq today)) and
                        ((RecommendationsTable.endDate.isNull()) or (RecommendationsTable.endDate greaterEq today))
                }.orderBy(RecommendationsTable.sortOrder to SortOrder.ASC, RecommendationsTable.id to SortOrder.ASC)
                .limit(limit)
                .map(::toRecord)
        }

    override fun listPersonalizedRecommendations(
        userId: Int,
        limit: Int,
    ): List<RecommendationRecord> =
        databaseFactory.withTransaction {
            val viewedProductIds =
                RecentProductViewsTable.selectAll()
                    .where { RecentProductViewsTable.user eq userId }
                    .orderBy(RecentProductViewsTable.viewedAt to SortOrder.DESC)
                    .map { it[RecentProductViewsTable.product].value }
            val wishedProductIds =
                WishlistsTable.selectAll()
                    .where { WishlistsTable.user eq userId }
                    .map { it[WishlistsTable.product].value }
            val prioritizedIds = (viewedProductIds + wishedProductIds).distinct().take(limit)
            if (prioritizedIds.isEmpty()) {
                listTodayRecommendations(limit)
            } else {
                ProductsTable.selectAll()
                    .where { ProductsTable.id inList prioritizedIds }
                    .map { row ->
                        val productId = row[ProductsTable.id].value
                        RecommendationRecord(
                            id = productId,
                            productId = productId,
                            productName = row[ProductsTable.name],
                            thumbnailUrl = row[ProductsTable.thumbnailUrl],
                            lowestPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
                            type = RecommendationType.TODAY,
                            sortOrder = prioritizedIds.indexOf(productId),
                            startDate = null,
                            endDate = null,
                            createdAt = row[ProductsTable.createdAt],
                        )
                    }.sortedBy { it.sortOrder }
            }
        }

    override fun listAdminRecommendations(): List<RecommendationRecord> =
        databaseFactory.withTransaction {
            RecommendationsTable.innerJoin(ProductsTable)
                .selectAll()
                .orderBy(RecommendationsTable.sortOrder to SortOrder.ASC, RecommendationsTable.id to SortOrder.ASC)
                .map(::toRecord)
        }

    override fun createRecommendation(newRecommendation: NewRecommendation): RecommendationRecord =
        databaseFactory.withTransaction {
            val insertedId =
                RecommendationsTable.insertAndGetId { row ->
                    row[product] = EntityID(newRecommendation.productId, ProductsTable)
                    row[type] = newRecommendation.type
                    row[sortOrder] = newRecommendation.sortOrder
                    row[startDate] = newRecommendation.startDate
                    row[endDate] = newRecommendation.endDate
                    row[createdAt] = Instant.now()
                }.value
            RecommendationsTable.innerJoin(ProductsTable)
                .selectAll()
                .where { RecommendationsTable.id eq insertedId }
                .single()
                .let(::toRecord)
        }

    override fun deleteRecommendation(id: Int) {
        databaseFactory.withTransaction {
            RecommendationsTable.deleteWhere { RecommendationsTable.id eq id }
        }
    }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.limit(1).any()
        }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): RecommendationRecord =
        RecommendationRecord(
            id = row[RecommendationsTable.id].value,
            productId = row[RecommendationsTable.product].value,
            productName = row[ProductsTable.name],
            thumbnailUrl = row[ProductsTable.thumbnailUrl],
            lowestPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
            type = row[RecommendationsTable.type],
            sortOrder = row[RecommendationsTable.sortOrder],
            startDate = row[RecommendationsTable.startDate],
            endDate = row[RecommendationsTable.endDate],
            createdAt = row[RecommendationsTable.createdAt],
        )
}
