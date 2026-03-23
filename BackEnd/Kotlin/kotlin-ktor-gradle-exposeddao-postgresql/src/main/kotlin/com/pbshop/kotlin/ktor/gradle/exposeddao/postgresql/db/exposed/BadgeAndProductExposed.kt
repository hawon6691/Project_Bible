package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.id.IntIdTable

object ProductsTable : IntIdTable("products") {
    val category = reference("category_id", CategoriesTable)
}

object BadgesTable : IntIdTable("badges") {
    val name = varchar("name", 50)
    val iconUrl = varchar("icon_url", 500)
}

object UserBadgesTable : IntIdTable("user_badges") {
    val user = reference("user_id", UsersTable)
    val badge = reference("badge_id", BadgesTable)
}
