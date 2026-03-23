package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import java.sql.ResultSet

class JdbcCategoryRepository(
    private val databaseFactory: DatabaseFactory,
) : CategoryRepository {
    override fun listCategories(): List<CategoryRecord> =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, name, parent_id, sort_order
                FROM categories
                ORDER BY sort_order ASC, id ASC
                """.trimIndent(),
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.toCategoryRecord())
                        }
                    }
                }
            }
        }

    override fun findCategoryById(id: Int): CategoryRecord? =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, name, parent_id, sort_order
                FROM categories
                WHERE id = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toCategoryRecord() else null
                }
            }
        }

    override fun createCategory(newCategory: NewCategory): CategoryRecord =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO categories (name, parent_id, sort_order)
                VALUES (?, ?, ?)
                RETURNING id, name, parent_id, sort_order
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, newCategory.name)
                statement.setObject(2, newCategory.parentId)
                statement.setInt(3, newCategory.sortOrder)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.toCategoryRecord()
                }
            }
        }

    override fun updateCategory(
        categoryId: Int,
        update: CategoryUpdate,
    ): CategoryRecord =
        databaseFactory.withConnection { connection ->
            val current = requireNotNull(findCategoryById(categoryId)) { "Category $categoryId not found" }
            connection.prepareStatement(
                """
                UPDATE categories
                SET name = ?,
                    sort_order = ?,
                    updated_at = NOW()
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, update.name ?: current.name)
                statement.setInt(2, update.sortOrder ?: current.sortOrder)
                statement.setInt(3, categoryId)
                statement.executeUpdate()
            }
            requireNotNull(findCategoryById(categoryId))
        }

    override fun hasChildren(categoryId: Int): Boolean =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                "SELECT EXISTS(SELECT 1 FROM categories WHERE parent_id = ?)",
            ).use { statement ->
                statement.setInt(1, categoryId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getBoolean(1)
                }
            }
        }

    override fun hasProducts(categoryId: Int): Boolean =
        databaseFactory.withConnection { connection ->
            connection.prepareStatement(
                "SELECT EXISTS(SELECT 1 FROM products WHERE category_id = ?)",
            ).use { statement ->
                statement.setInt(1, categoryId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getBoolean(1)
                }
            }
        }

    override fun deleteCategory(categoryId: Int) {
        databaseFactory.withConnection { connection ->
            connection.prepareStatement("DELETE FROM categories WHERE id = ?").use { statement ->
                statement.setInt(1, categoryId)
                statement.executeUpdate()
            }
        }
    }

    private fun ResultSet.toCategoryRecord(): CategoryRecord =
        CategoryRecord(
            id = getInt("id"),
            name = getString("name"),
            parentId = getObject("parent_id") as Int?,
            sortOrder = getInt("sort_order"),
        )
}
