package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class NewsService(
    private val repository: NewsRepository,
) {
    fun categories(): StubResponse = StubResponse(data = repository.listCategories().map(::categoryPayload))

    fun createCategory(request: CreateNewsCategoryRequest): StubResponse {
        if (request.name.trim().isBlank() || request.slug.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "name과 slug가 필요합니다.")
        }
        if (repository.findCategoryByNameOrSlug(request.name.trim(), request.slug.trim()) != null) {
            throw PbShopException(HttpStatusCode.Conflict, "VALIDATION_ERROR", "이미 존재하는 카테고리입니다.")
        }
        val created = repository.createCategory(NewNewsCategory(request.name.trim(), request.slug.trim()))
        return StubResponse(status = HttpStatusCode.Created, data = categoryPayload(created))
    }

    fun deleteCategory(categoryId: Int): StubResponse {
        requireCategory(categoryId)
        if (repository.categoryHasNews(categoryId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "게시된 뉴스가 있어 삭제할 수 없습니다.")
        }
        repository.deleteCategory(categoryId)
        return StubResponse(data = mapOf("success" to true, "message" to "뉴스 카테고리가 삭제되었습니다."))
    }

    fun list(category: String?, page: Int, limit: Int): StubResponse {
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listNews(category?.trim()?.takeIf { it.isNotBlank() }, normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::summaryPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun detail(newsId: Int): StubResponse {
        val news =
            repository.incrementViewCount(newsId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "뉴스를 찾을 수 없습니다.")
        return StubResponse(data = detailPayload(news))
    }

    fun create(request: CreateNewsRequest): StubResponse {
        validateNewsRequest(request.title, request.content)
        requireCategory(request.categoryId)
        ensureProducts(request.productIds)
        val created =
            repository.createNews(
                NewNews(
                    title = request.title.trim(),
                    content = request.content.trim(),
                    categoryId = request.categoryId,
                    thumbnailUrl = request.thumbnailUrl,
                    productIds = request.productIds.distinct(),
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = detailPayload(created))
    }

    fun update(newsId: Int, request: UpdateNewsRequest): StubResponse {
        requireNews(newsId)
        request.categoryId?.let(::requireCategory)
        request.productIds?.let(::ensureProducts)
        if (request.title != null || request.content != null) {
            validateNewsRequest(request.title ?: "filled", request.content ?: "filled")
        }
        val updated =
            repository.updateNews(
                newsId,
                NewsUpdate(
                    title = request.title?.trim(),
                    content = request.content?.trim(),
                    categoryId = request.categoryId,
                    thumbnailUrl = request.thumbnailUrl,
                    productIds = request.productIds?.distinct(),
                ),
            )
        return StubResponse(data = detailPayload(updated))
    }

    fun delete(newsId: Int): StubResponse {
        if (!repository.deleteNews(newsId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "뉴스를 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("success" to true, "message" to "뉴스가 삭제되었습니다."))
    }

    private fun validateNewsRequest(title: String, content: String) {
        if (title.trim().isBlank() || content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title과 content가 필요합니다.")
        }
    }

    private fun requireCategory(categoryId: Int): NewsCategoryRecord =
        repository.findCategoryById(categoryId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "카테고리를 찾을 수 없습니다.")

    private fun requireNews(newsId: Int): NewsRecord =
        repository.findNewsById(newsId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "뉴스를 찾을 수 없습니다.")

    private fun ensureProducts(productIds: List<Int>) {
        productIds.forEach {
            if (!repository.productExists(it)) {
                throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
            }
        }
    }

    private fun categoryPayload(record: NewsCategoryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "slug" to record.slug,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun summaryPayload(record: NewsRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "title" to record.title,
            "thumbnailUrl" to record.thumbnailUrl,
            "category" to record.category?.let(::categoryPayload),
            "viewCount" to record.viewCount,
            "createdAt" to record.createdAt.toString(),
        )

    private fun detailPayload(record: NewsRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "title" to record.title,
            "content" to record.content,
            "thumbnailUrl" to record.thumbnailUrl,
            "category" to record.category?.let(::categoryPayload),
            "viewCount" to record.viewCount,
            "relatedProducts" to
                record.relatedProducts.map {
                    mapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "thumbnailUrl" to it.thumbnailUrl,
                        "lowestPrice" to it.lowestPrice,
                    )
                },
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun normalizePage(page: Int): Int = if (page > 0) page else 1

    private fun normalizeLimit(limit: Int): Int = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20

    private fun pageMeta(page: Int, limit: Int, totalCount: Int): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount - 1) / limit) + 1,
        )
}
