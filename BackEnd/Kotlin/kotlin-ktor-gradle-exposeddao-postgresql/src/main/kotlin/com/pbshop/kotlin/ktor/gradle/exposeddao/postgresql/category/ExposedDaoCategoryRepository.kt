package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoryEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoCategoryRepository(
    private val databaseFactory: DatabaseFactory,
) : CategoryRepository {
    override fun listCategories(): List<CategoryRecord> =
        databaseFactory.withTransaction {
            CategoryEntity.all()
                .orderBy(CategoriesTable.sortOrder to SortOrder.ASC, CategoriesTable.id to SortOrder.ASC)
                .map(::toCategoryRecord)
        }

    override fun findCategoryById(id: Int): CategoryRecord? =
        databaseFactory.withTransaction {
            CategoryEntity.findById(id)?.let(::toCategoryRecord)
        }

    override fun createCategory(newCategory: NewCategory): CategoryRecord =
        databaseFactory.withTransaction {
            CategoryEntity.new {
                name = newCategory.name
                parentId = newCategory.parentId?.let { parentId -> EntityID(parentId, CategoriesTable) }
                sortOrder = newCategory.sortOrder
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.let(::toCategoryRecord)
        }

    override fun updateCategory(
        categoryId: Int,
        update: CategoryUpdate,
    ): CategoryRecord =
        databaseFactory.withTransaction {
            val category = requireNotNull(CategoryEntity.findById(categoryId)) { "Category $categoryId not found" }
            category.apply {
                name = update.name ?: name
                sortOrder = update.sortOrder ?: sortOrder
                updatedAt = Instant.now()
            }.let(::toCategoryRecord)
        }

    override fun hasChildren(categoryId: Int): Boolean =
        databaseFactory.withTransaction {
            !CategoriesTable
                .selectAll()
                .where { CategoriesTable.parent eq categoryId }
                .limit(1)
                .empty()
        }

    override fun hasProducts(categoryId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable
                .selectAll()
                .where { ProductsTable.category eq categoryId }
                .limit(1)
                .empty()
        }

    override fun deleteCategory(categoryId: Int) {
        databaseFactory.withTransaction {
            CategoryEntity.findById(categoryId)?.delete()
        }
    }

    private fun toCategoryRecord(entity: CategoryEntity): CategoryRecord =
        CategoryRecord(
            id = entity.id.value,
            name = entity.name,
            parentId = entity.parentId?.value,
            sortOrder = entity.sortOrder,
        )
}
