package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrderItemsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ReviewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecentProductViewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SearchLogsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class ExposedDaoRankingRepository(
    private val databaseFactory: DatabaseFactory,
) : RankingRepository {
    override fun listPopularProducts(
        categoryId: Int?,
        period: RankingPeriod,
        limit: Int,
    ): List<PopularProductRanking> =
        databaseFactory.withTransaction {
            val threshold = period.threshold()
            val baseCondition =
                if (categoryId != null) {
                    (ProductsTable.deletedAt.isNull()) and (ProductsTable.category eq categoryId)
                } else {
                    ProductsTable.deletedAt.isNull()
                }

            val products =
                ProductsTable.selectAll()
                    .where { baseCondition }
                    .map { row ->
                        val productId = row[ProductsTable.id].value
                        val views =
                            RecentProductViewsTable.selectAll()
                                .where { (RecentProductViewsTable.product eq productId) and (RecentProductViewsTable.viewedAt greaterEq threshold) }
                                .count()
                                .toInt()
                        val sales =
                            OrderItemsTable.selectAll()
                                .where { OrderItemsTable.product eq productId }
                                .count()
                                .toInt()
                        val reviews =
                            ReviewsTable.selectAll()
                                .where { (ReviewsTable.product eq productId) and ReviewsTable.deletedAt.isNull() }
                                .count()
                                .toInt()
                        val score = (views * 3) + (sales * 5) + (reviews * 2)
                        Triple(row, score, views)
                    }.sortedByDescending { it.second }
                    .take(limit)

            products.mapIndexed { index, triple ->
                val row = triple.first
                PopularProductRanking(
                    rank = index + 1,
                    productId = row[ProductsTable.id].value,
                    productName = row[ProductsTable.name],
                    thumbnailUrl = row[ProductsTable.thumbnailUrl],
                    lowestPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
                    score = triple.second,
                    rankChange = 0,
                )
            }
        }

    override fun listPopularSearches(
        period: RankingPeriod,
        limit: Int,
    ): List<PopularSearchRanking> =
        databaseFactory.withTransaction {
            val threshold = period.threshold()
            SearchLogsTable.selectAll()
                .where { SearchLogsTable.searchedAt greaterEq threshold }
                .groupBy { it[SearchLogsTable.keyword] }
                .map { (keyword, rows) ->
                    keyword to rows.size
                }.sortedByDescending { it.second }
                .take(limit)
                .mapIndexed { index, (keyword, count) ->
                    PopularSearchRanking(index + 1, keyword, count, 0)
                }
        }
}
