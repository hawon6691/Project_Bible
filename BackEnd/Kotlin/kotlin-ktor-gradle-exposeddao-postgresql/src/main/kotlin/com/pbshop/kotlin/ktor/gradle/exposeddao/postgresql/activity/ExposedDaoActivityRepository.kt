package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecentProductViewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SearchHistoriesTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class ExposedDaoActivityRepository(
    private val databaseFactory: DatabaseFactory,
) : ActivityRepository {
    override fun listViewHistory(
        userId: Int,
        page: Int,
        limit: Int,
    ): ViewHistoryListResult =
        databaseFactory.withTransaction {
            val rows =
                RecentProductViewsTable
                    .join(ProductsTable, org.jetbrains.exposed.sql.JoinType.INNER, RecentProductViewsTable.product, ProductsTable.id)
                    .selectAll()
                    .where { RecentProductViewsTable.user eq userId }
                    .orderBy(RecentProductViewsTable.viewedAt to SortOrder.DESC, RecentProductViewsTable.id to SortOrder.DESC)
                    .map {
                        ViewHistoryRecord(
                            id = it[RecentProductViewsTable.id].value,
                            userId = it[RecentProductViewsTable.user].value,
                            productId = it[RecentProductViewsTable.product].value,
                            productName = it[ProductsTable.name],
                            thumbnailUrl = it[ProductsTable.thumbnailUrl],
                            viewedAt = it[RecentProductViewsTable.viewedAt],
                        )
                    }
            val offset = (page - 1) * limit
            ViewHistoryListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun clearViewHistory(userId: Int) {
        databaseFactory.withTransaction {
            RecentProductViewsTable.deleteWhere { RecentProductViewsTable.user eq userId }
        }
    }

    override fun listSearchHistories(
        userId: Int,
        page: Int,
        limit: Int,
    ): SearchHistoryListResult =
        databaseFactory.withTransaction {
            val rows =
                SearchHistoriesTable
                    .selectAll()
                    .where { SearchHistoriesTable.user eq userId }
                    .orderBy(SearchHistoriesTable.createdAt to SortOrder.DESC, SearchHistoriesTable.id to SortOrder.DESC)
                    .map {
                        SearchHistoryRecord(
                            id = it[SearchHistoriesTable.id].value,
                            userId = it[SearchHistoriesTable.user].value,
                            keyword = it[SearchHistoriesTable.keyword],
                            createdAt = it[SearchHistoriesTable.createdAt],
                        )
                    }
            val offset = (page - 1) * limit
            SearchHistoryListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findSearchHistoryById(id: Int): SearchHistoryRecord? =
        databaseFactory.withTransaction {
            SearchHistoriesTable
                .selectAll()
                .where { SearchHistoriesTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let {
                    SearchHistoryRecord(
                        id = it[SearchHistoriesTable.id].value,
                        userId = it[SearchHistoriesTable.user].value,
                        keyword = it[SearchHistoriesTable.keyword],
                        createdAt = it[SearchHistoriesTable.createdAt],
                    )
                }
        }

    override fun clearSearchHistories(userId: Int) {
        databaseFactory.withTransaction {
            SearchHistoriesTable.deleteWhere { SearchHistoriesTable.user eq userId }
        }
    }

    override fun deleteSearchHistory(id: Int) {
        databaseFactory.withTransaction {
            SearchHistoriesTable.deleteWhere { SearchHistoriesTable.id eq id }
        }
    }
}
