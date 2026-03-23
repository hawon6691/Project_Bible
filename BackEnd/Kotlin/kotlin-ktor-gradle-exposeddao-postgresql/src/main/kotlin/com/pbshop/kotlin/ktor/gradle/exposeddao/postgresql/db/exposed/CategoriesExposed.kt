package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object CategoriesTable : IntIdTable("categories") {
    val name = varchar("name", 50)
    val parent = optReference("parent_id", CategoriesTable)
    val sortOrder = integer("sort_order")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class CategoryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CategoryEntity>(CategoriesTable)

    var name by CategoriesTable.name
    var parentId by CategoriesTable.parent
    var sortOrder by CategoriesTable.sortOrder
    var createdAt by CategoriesTable.createdAt
    var updatedAt by CategoriesTable.updatedAt
}
