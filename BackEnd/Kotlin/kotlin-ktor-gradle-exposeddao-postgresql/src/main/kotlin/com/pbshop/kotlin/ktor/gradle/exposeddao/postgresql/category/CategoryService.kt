package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class CategoryService(
    private val repository: CategoryRepository,
) {
    fun tree(): StubResponse = StubResponse(data = buildTree(repository.listCategories(), parentId = null))

    fun detail(categoryId: Int): StubResponse {
        val categories = repository.listCategories()
        val category =
            categories.firstOrNull { it.id == categoryId }
                ?: throw PbShopException(HttpStatusCode.NotFound, "CATEGORY_NOT_FOUND", "해당 카테고리를 찾을 수 없습니다.")
        return StubResponse(data = categoryPayload(category, categories))
    }

    fun create(request: CategoryCreateRequest): StubResponse {
        val name = request.name.trim()
        if (name.isBlank() || name.length > 50) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "카테고리 이름은 1자 이상 50자 이하로 입력해주세요.")
        }
        if (request.parentId != null && repository.findCategoryById(request.parentId) == null) {
            throw PbShopException(HttpStatusCode.BadRequest, "CATEGORY_PARENT_NOT_FOUND", "상위 카테고리를 찾을 수 없습니다.")
        }

        val sortOrder =
            request.sortOrder
                ?: repository.listCategories()
                    .filter { it.parentId == request.parentId }
                    .maxOfOrNull { it.sortOrder + 1 }
                ?: 1

        val created =
            repository.createCategory(
                NewCategory(
                    name = name,
                    parentId = request.parentId,
                    sortOrder = sortOrder,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = categoryPayload(created, repository.listCategories()))
    }

    fun update(
        categoryId: Int,
        request: CategoryUpdateRequest,
    ): StubResponse {
        if (request.name == null && request.sortOrder == null) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 값이 없습니다.")
        }
        request.name?.let {
            val normalized = it.trim()
            if (normalized.isBlank() || normalized.length > 50) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "카테고리 이름은 1자 이상 50자 이하로 입력해주세요.")
            }
        }
        request.sortOrder?.let {
            if (it < 0) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "sortOrder는 0 이상이어야 합니다.")
            }
        }

        val updated =
            repository.updateCategory(
                categoryId = categoryId,
                update =
                    CategoryUpdate(
                        name = request.name?.trim(),
                        sortOrder = request.sortOrder,
                    ),
            )
        return StubResponse(data = categoryPayload(updated, repository.listCategories()))
    }

    fun delete(categoryId: Int): StubResponse {
        val category =
            repository.findCategoryById(categoryId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "CATEGORY_NOT_FOUND", "해당 카테고리를 찾을 수 없습니다.")

        if (repository.hasChildren(category.id)) {
            throw PbShopException(HttpStatusCode.Conflict, "CATEGORY_HAS_CHILDREN", "하위 카테고리가 있는 카테고리는 삭제할 수 없습니다.")
        }
        if (repository.hasProducts(category.id)) {
            throw PbShopException(HttpStatusCode.Conflict, "CATEGORY_IN_USE", "상품이 연결된 카테고리는 삭제할 수 없습니다.")
        }

        repository.deleteCategory(category.id)
        return StubResponse(data = mapOf("message" to "카테고리가 삭제되었습니다."))
    }

    private fun buildTree(
        categories: List<CategoryRecord>,
        parentId: Int?,
    ): List<Map<String, Any?>> =
        categories
            .filter { it.parentId == parentId }
            .sortedWith(compareBy<CategoryRecord> { it.sortOrder }.thenBy { it.id })
            .map { category -> categoryPayload(category, categories) }

    private fun categoryPayload(
        category: CategoryRecord,
        categories: List<CategoryRecord>,
    ): Map<String, Any?> =
        mapOf(
            "id" to category.id,
            "name" to category.name,
            "parentId" to category.parentId,
            "sortOrder" to category.sortOrder,
            "children" to buildTree(categories, category.id),
        )
}
