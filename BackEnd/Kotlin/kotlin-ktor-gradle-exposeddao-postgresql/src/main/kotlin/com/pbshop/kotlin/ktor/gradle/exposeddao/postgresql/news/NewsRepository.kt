package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import java.time.Instant

data class NewsCategoryRecord(
    val id: Int,
    val name: String,
    val slug: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NewsRelatedProductRecord(
    val id: Int,
    val name: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
)

data class NewsRecord(
    val id: Int,
    val title: String,
    val content: String,
    val categoryId: Int,
    val thumbnailUrl: String?,
    val viewCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val category: NewsCategoryRecord?,
    val relatedProducts: List<NewsRelatedProductRecord>,
)

data class NewsListResult(
    val items: List<NewsRecord>,
    val totalCount: Int,
)

data class NewNews(
    val title: String,
    val content: String,
    val categoryId: Int,
    val thumbnailUrl: String?,
    val productIds: List<Int>,
)

data class NewsUpdate(
    val title: String? = null,
    val content: String? = null,
    val categoryId: Int? = null,
    val thumbnailUrl: String? = null,
    val productIds: List<Int>? = null,
)

data class NewNewsCategory(
    val name: String,
    val slug: String,
)

interface NewsRepository {
    fun listCategories(): List<NewsCategoryRecord>

    fun findCategoryById(id: Int): NewsCategoryRecord?

    fun findCategoryByNameOrSlug(name: String, slug: String): NewsCategoryRecord?

    fun createCategory(newCategory: NewNewsCategory): NewsCategoryRecord

    fun deleteCategory(categoryId: Int): Boolean

    fun categoryHasNews(categoryId: Int): Boolean

    fun productExists(productId: Int): Boolean

    fun listNews(category: String?, page: Int, limit: Int): NewsListResult

    fun findNewsById(newsId: Int): NewsRecord?

    fun incrementViewCount(newsId: Int): NewsRecord?

    fun createNews(newNews: NewNews): NewsRecord

    fun updateNews(newsId: Int, update: NewsUpdate): NewsRecord

    fun deleteNews(newsId: Int): Boolean
}
