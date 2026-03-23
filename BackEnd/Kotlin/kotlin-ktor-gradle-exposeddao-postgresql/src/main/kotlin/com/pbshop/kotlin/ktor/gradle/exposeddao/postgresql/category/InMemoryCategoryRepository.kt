package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

class InMemoryCategoryRepository(
    seededCategories: List<CategoryRecord> = emptyList(),
    private val categoryIdsInUseByProducts: Set<Int> = emptySet(),
) : CategoryRepository {
    private val categories = linkedMapOf<Int, CategoryRecord>()
    private var nextId = 1

    init {
        seededCategories.forEach {
            categories[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun listCategories(): List<CategoryRecord> = categories.values.sortedBy { it.id }

    override fun findCategoryById(id: Int): CategoryRecord? = categories[id]

    override fun createCategory(newCategory: NewCategory): CategoryRecord {
        val created =
            CategoryRecord(
                id = nextId++,
                name = newCategory.name,
                parentId = newCategory.parentId,
                sortOrder = newCategory.sortOrder,
            )
        categories[created.id] = created
        return created
    }

    override fun updateCategory(
        categoryId: Int,
        update: CategoryUpdate,
    ): CategoryRecord {
        val current = categories[categoryId] ?: error("Category $categoryId not found")
        val updated =
            current.copy(
                name = update.name ?: current.name,
                sortOrder = update.sortOrder ?: current.sortOrder,
            )
        categories[categoryId] = updated
        return updated
    }

    override fun hasChildren(categoryId: Int): Boolean = categories.values.any { it.parentId == categoryId }

    override fun hasProducts(categoryId: Int): Boolean = categoryIdsInUseByProducts.contains(categoryId)

    override fun deleteCategory(categoryId: Int) {
        categories.remove(categoryId)
    }

    companion object {
        fun seeded(): InMemoryCategoryRepository =
            InMemoryCategoryRepository(
                seededCategories =
                    listOf(
                        CategoryRecord(1, "컴퓨터", null, 1),
                        CategoryRecord(2, "노트북", 1, 1),
                        CategoryRecord(3, "데스크탑", 1, 2),
                        CategoryRecord(4, "자동차", null, 2),
                        CategoryRecord(5, "전기차", 4, 1),
                    ),
                categoryIdsInUseByProducts = setOf(2, 3, 4, 5),
            )
    }
}
