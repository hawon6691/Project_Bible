package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

data class CategoryRecord(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val sortOrder: Int,
)

data class NewCategory(
    val name: String,
    val parentId: Int?,
    val sortOrder: Int,
)

data class CategoryUpdate(
    val name: String?,
    val sortOrder: Int?,
)

interface CategoryRepository {
    fun listCategories(): List<CategoryRecord>

    fun findCategoryById(id: Int): CategoryRecord?

    fun createCategory(newCategory: NewCategory): CategoryRecord

    fun updateCategory(
        categoryId: Int,
        update: CategoryUpdate,
    ): CategoryRecord

    fun hasChildren(categoryId: Int): Boolean

    fun hasProducts(categoryId: Int): Boolean

    fun deleteCategory(categoryId: Int)
}
