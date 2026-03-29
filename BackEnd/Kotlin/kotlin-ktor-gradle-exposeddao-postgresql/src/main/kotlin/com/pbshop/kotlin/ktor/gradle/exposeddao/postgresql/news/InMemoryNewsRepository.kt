package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import java.time.Instant

class InMemoryNewsRepository(
    categories: List<NewsCategoryRecord>,
    newsItems: List<NewsRecord>,
    productCatalog: Map<Int, NewsRelatedProductRecord>,
) : NewsRepository {
    private val categories = linkedMapOf<Int, NewsCategoryRecord>()
    private val news = linkedMapOf<Int, NewsRecord>()
    private val products = productCatalog.toMutableMap()
    private val deletedCategoryIds = mutableSetOf<Int>()
    private val deletedNewsIds = mutableSetOf<Int>()
    private var categorySequence = 1
    private var newsSequence = 1

    init {
        categories.forEach {
            this.categories[it.id] = it
            categorySequence = maxOf(categorySequence, it.id + 1)
        }
        newsItems.forEach {
            this.news[it.id] = it
            newsSequence = maxOf(newsSequence, it.id + 1)
        }
    }

    override fun listCategories(): List<NewsCategoryRecord> = categories.values.filterNot { deletedCategoryIds.contains(it.id) }.sortedBy { it.id }

    override fun findCategoryById(id: Int): NewsCategoryRecord? = categories[id]?.takeIf { !deletedCategoryIds.contains(id) }

    override fun findCategoryByNameOrSlug(name: String, slug: String): NewsCategoryRecord? =
        categories.values.firstOrNull { !deletedCategoryIds.contains(it.id) && (it.name == name || it.slug == slug) }

    override fun createCategory(newCategory: NewNewsCategory): NewsCategoryRecord {
        val now = Instant.now()
        return NewsCategoryRecord(categorySequence++, newCategory.name, newCategory.slug, now, now).also { categories[it.id] = it }
    }

    override fun deleteCategory(categoryId: Int): Boolean {
        if (!categories.containsKey(categoryId) || deletedCategoryIds.contains(categoryId)) return false
        deletedCategoryIds += categoryId
        return true
    }

    override fun categoryHasNews(categoryId: Int): Boolean = news.values.any { !deletedNewsIds.contains(it.id) && it.categoryId == categoryId }

    override fun productExists(productId: Int): Boolean = products.containsKey(productId)

    override fun listNews(category: String?, page: Int, limit: Int): NewsListResult {
        val filtered =
            news.values
                .filterNot { deletedNewsIds.contains(it.id) }
                .filter { item ->
                    category.isNullOrBlank() ||
                        item.category?.name == category ||
                        item.category?.slug == category
                }
                .sortedByDescending { it.createdAt }
        val fromIndex = ((page - 1) * limit).coerceAtMost(filtered.size)
        val toIndex = (fromIndex + limit).coerceAtMost(filtered.size)
        return NewsListResult(filtered.subList(fromIndex, toIndex), filtered.size)
    }

    override fun findNewsById(newsId: Int): NewsRecord? = news[newsId]?.takeIf { !deletedNewsIds.contains(newsId) }

    override fun incrementViewCount(newsId: Int): NewsRecord? {
        val current = findNewsById(newsId) ?: return null
        val updated = current.copy(viewCount = current.viewCount + 1, updatedAt = Instant.now())
        news[newsId] = updated
        return updated
    }

    override fun createNews(newNews: NewNews): NewsRecord {
        val now = Instant.now()
        val category = categories[newNews.categoryId]
        val record =
            NewsRecord(
                id = newsSequence++,
                title = newNews.title,
                content = newNews.content,
                categoryId = newNews.categoryId,
                thumbnailUrl = newNews.thumbnailUrl,
                viewCount = 0,
                createdAt = now,
                updatedAt = now,
                category = category,
                relatedProducts = newNews.productIds.mapNotNull(products::get),
            )
        news[record.id] = record
        return record
    }

    override fun updateNews(newsId: Int, update: NewsUpdate): NewsRecord {
        val current = news[newsId] ?: error("News $newsId not found")
        val categoryId = update.categoryId ?: current.categoryId
        val category = categories[categoryId]
        val updated =
            current.copy(
                title = update.title ?: current.title,
                content = update.content ?: current.content,
                categoryId = categoryId,
                thumbnailUrl = update.thumbnailUrl ?: current.thumbnailUrl,
                updatedAt = Instant.now(),
                category = category,
                relatedProducts = update.productIds?.mapNotNull(products::get) ?: current.relatedProducts,
            )
        news[newsId] = updated
        return updated
    }

    override fun deleteNews(newsId: Int): Boolean {
        if (!news.containsKey(newsId) || deletedNewsIds.contains(newsId)) return false
        deletedNewsIds += newsId
        return true
    }

    companion object {
        fun seeded(): InMemoryNewsRepository {
            val now = Instant.parse("2026-03-20T11:00:00Z")
            val categories =
                listOf(
                    NewsCategoryRecord(1, "리뷰", "reviews", now, now),
                    NewsCategoryRecord(2, "이벤트", "events", now, now),
                )
            val products =
                mapOf(
                    1 to NewsRelatedProductRecord(1, "게이밍 노트북 A15", "https://img.example.com/a15.jpg", 1_490_000),
                    2 to NewsRelatedProductRecord(2, "게이밍 노트북 B17", "https://img.example.com/b17.jpg", 1_890_000),
                    3 to NewsRelatedProductRecord(3, "태블릿 Pro 11", "https://img.example.com/tablet.jpg", 790_000),
                )
            val newsItems =
                listOf(
                    NewsRecord(1, "A15 출시 리뷰", "성능과 발열을 점검했습니다.", 1, "/uploads/news/a15.jpg", 12, now, now, categories[0], listOf(products[1]!!)),
                    NewsRecord(2, "주말 할인 이벤트", "태블릿 한정 특가 소식입니다.", 2, "/uploads/news/weekend.jpg", 8, now.minusSeconds(3600), now.minusSeconds(3600), categories[1], listOf(products[3]!!)),
                )
            return InMemoryNewsRepository(categories, newsItems, products)
        }
    }
}
