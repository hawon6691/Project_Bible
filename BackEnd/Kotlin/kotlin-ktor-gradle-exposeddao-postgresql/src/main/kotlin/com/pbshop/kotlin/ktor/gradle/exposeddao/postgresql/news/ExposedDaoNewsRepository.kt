package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.NewsCategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.NewsProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.NewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoNewsRepository(
    private val databaseFactory: DatabaseFactory,
) : NewsRepository {
    override fun listCategories(): List<NewsCategoryRecord> =
        databaseFactory.withTransaction {
            NewsCategoriesTable.selectAll()
                .where { NewsCategoriesTable.deletedAt.isNull() }
                .orderBy(NewsCategoriesTable.id to SortOrder.ASC)
                .map(::toCategoryRecord)
        }

    override fun findCategoryById(id: Int): NewsCategoryRecord? =
        databaseFactory.withTransaction {
            NewsCategoriesTable.selectAll()
                .where { (NewsCategoriesTable.id eq id) and NewsCategoriesTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let(::toCategoryRecord)
        }

    override fun findCategoryByNameOrSlug(name: String, slug: String): NewsCategoryRecord? =
        databaseFactory.withTransaction {
            NewsCategoriesTable.selectAll()
                .where { ((NewsCategoriesTable.name eq name) or (NewsCategoriesTable.slug eq slug)) and NewsCategoriesTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let(::toCategoryRecord)
        }

    override fun createCategory(newCategory: NewNewsCategory): NewsCategoryRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val insertedId =
                NewsCategoriesTable.insert {
                    it[NewsCategoriesTable.name] = newCategory.name
                    it[NewsCategoriesTable.slug] = newCategory.slug
                    it[NewsCategoriesTable.deletedAt] = null
                    it[NewsCategoriesTable.createdAt] = now
                    it[NewsCategoriesTable.updatedAt] = now
                } get NewsCategoriesTable.id
            NewsCategoryRecord(insertedId.value, newCategory.name, newCategory.slug, now, now)
        }

    override fun deleteCategory(categoryId: Int): Boolean =
        databaseFactory.withTransaction {
            NewsCategoriesTable.update({ (NewsCategoriesTable.id eq categoryId) and NewsCategoriesTable.deletedAt.isNull() }) {
                it[NewsCategoriesTable.deletedAt] = Instant.now()
                it[NewsCategoriesTable.updatedAt] = Instant.now()
            } > 0
        }

    override fun categoryHasNews(categoryId: Int): Boolean =
        databaseFactory.withTransaction {
            !NewsTable.selectAll().where { (NewsTable.categoryId eq categoryId) and NewsTable.deletedAt.isNull() }.empty()
        }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.empty()
        }

    override fun listNews(category: String?, page: Int, limit: Int): NewsListResult =
        databaseFactory.withTransaction {
            val categoriesById = listCategories().associateBy { it.id }
            val rows =
                NewsTable.selectAll()
                    .where { NewsTable.deletedAt.isNull() }
                    .orderBy(NewsTable.createdAt to SortOrder.DESC)
                    .map { row -> rowToNews(row, categoriesById[row[NewsTable.categoryId]], includeProducts = false) }
                    .filter { item ->
                        category.isNullOrBlank() ||
                            item.category?.slug == category ||
                            item.category?.name == category
                    }
            val fromIndex = ((page - 1) * limit).coerceAtMost(rows.size)
            val toIndex = (fromIndex + limit).coerceAtMost(rows.size)
            NewsListResult(rows.subList(fromIndex, toIndex), rows.size)
        }

    override fun findNewsById(newsId: Int): NewsRecord? =
        databaseFactory.withTransaction {
            val row =
                NewsTable.selectAll()
                    .where { (NewsTable.id eq newsId) and NewsTable.deletedAt.isNull() }
                    .singleOrNull()
                    ?: return@withTransaction null
            val category = findCategoryById(row[NewsTable.categoryId])
            rowToNews(row, category, includeProducts = true)
        }

    override fun incrementViewCount(newsId: Int): NewsRecord? =
        databaseFactory.withTransaction {
            val current =
                NewsTable.selectAll()
                    .where { (NewsTable.id eq newsId) and NewsTable.deletedAt.isNull() }
                    .singleOrNull()
                    ?: return@withTransaction null
            NewsTable.update({ NewsTable.id eq newsId }) {
                it[NewsTable.viewCount] = current[NewsTable.viewCount] + 1
                it[NewsTable.updatedAt] = Instant.now()
            }
            findNewsById(newsId)
        }

    override fun createNews(newNews: NewNews): NewsRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val insertedId =
                NewsTable.insert {
                    it[NewsTable.title] = newNews.title
                    it[NewsTable.content] = newNews.content
                    it[NewsTable.categoryId] = newNews.categoryId
                    it[NewsTable.thumbnailUrl] = newNews.thumbnailUrl
                    it[NewsTable.viewCount] = 0
                    it[NewsTable.deletedAt] = null
                    it[NewsTable.createdAt] = now
                    it[NewsTable.updatedAt] = now
                } get NewsTable.id
            syncNewsProducts(insertedId.value, newNews.productIds, now)
            findNewsById(insertedId.value)!!
        }

    override fun updateNews(newsId: Int, update: NewsUpdate): NewsRecord =
        databaseFactory.withTransaction {
            val current = findNewsById(newsId) ?: error("News $newsId not found")
            val now = Instant.now()
            NewsTable.update({ NewsTable.id eq newsId }) {
                it[NewsTable.title] = update.title ?: current.title
                it[NewsTable.content] = update.content ?: current.content
                it[NewsTable.categoryId] = update.categoryId ?: current.categoryId
                it[NewsTable.thumbnailUrl] = update.thumbnailUrl ?: current.thumbnailUrl
                it[NewsTable.updatedAt] = now
            }
            if (update.productIds != null) {
                NewsProductsTable.update({ (NewsProductsTable.newsId eq newsId) and NewsProductsTable.deletedAt.isNull() }) {
                    it[NewsProductsTable.deletedAt] = now
                    it[NewsProductsTable.updatedAt] = now
                }
                syncNewsProducts(newsId, update.productIds, now)
            }
            findNewsById(newsId)!!
        }

    override fun deleteNews(newsId: Int): Boolean =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val updated =
                NewsTable.update({ (NewsTable.id eq newsId) and NewsTable.deletedAt.isNull() }) {
                    it[NewsTable.deletedAt] = now
                    it[NewsTable.updatedAt] = now
                }
            if (updated > 0) {
                NewsProductsTable.update({ (NewsProductsTable.newsId eq newsId) and NewsProductsTable.deletedAt.isNull() }) {
                    it[NewsProductsTable.deletedAt] = now
                    it[NewsProductsTable.updatedAt] = now
                }
            }
            updated > 0
        }

    private fun syncNewsProducts(newsId: Int, productIds: List<Int>, now: Instant) {
        productIds.distinct().forEach { productId ->
            NewsProductsTable.insertIgnore {
                it[NewsProductsTable.newsId] = newsId
                it[NewsProductsTable.productId] = productId
                it[NewsProductsTable.deletedAt] = null
                it[NewsProductsTable.createdAt] = now
                it[NewsProductsTable.updatedAt] = now
            }
        }
    }

    private fun rowToNews(row: ResultRow, category: NewsCategoryRecord?, includeProducts: Boolean): NewsRecord {
        val relatedProducts =
            if (!includeProducts) {
                emptyList()
            } else {
                val productIds =
                    NewsProductsTable.selectAll()
                        .where { (NewsProductsTable.newsId eq row[NewsTable.id].value) and NewsProductsTable.deletedAt.isNull() }
                        .map { it[NewsProductsTable.productId] }
                ProductsTable.selectAll()
                    .where { ProductsTable.deletedAt.isNull() }
                    .filter { it[ProductsTable.id].value in productIds }
                    .map {
                        NewsRelatedProductRecord(
                            id = it[ProductsTable.id].value,
                            name = it[ProductsTable.name],
                            thumbnailUrl = it[ProductsTable.thumbnailUrl],
                            lowestPrice = it[ProductsTable.lowestPrice] ?: it[ProductsTable.price],
                        )
                    }
            }
        return NewsRecord(
            id = row[NewsTable.id].value,
            title = row[NewsTable.title],
            content = row[NewsTable.content],
            categoryId = row[NewsTable.categoryId],
            thumbnailUrl = row[NewsTable.thumbnailUrl],
            viewCount = row[NewsTable.viewCount],
            createdAt = row[NewsTable.createdAt],
            updatedAt = row[NewsTable.updatedAt],
            category = category,
            relatedProducts = relatedProducts,
        )
    }

    private fun toCategoryRecord(row: ResultRow): NewsCategoryRecord =
        NewsCategoryRecord(
            id = row[NewsCategoriesTable.id].value,
            name = row[NewsCategoriesTable.name],
            slug = row[NewsCategoriesTable.slug],
            createdAt = row[NewsCategoriesTable.createdAt],
            updatedAt = row[NewsCategoriesTable.updatedAt],
        )
}
