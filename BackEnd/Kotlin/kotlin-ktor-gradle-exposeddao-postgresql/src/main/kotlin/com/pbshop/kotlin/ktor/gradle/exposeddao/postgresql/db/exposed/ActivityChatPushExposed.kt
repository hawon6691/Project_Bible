package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object RecentProductViewsTable : IntIdTable("recent_product_views") {
    val user = reference("user_id", UsersTable)
    val product = reference("product_id", ProductsTable)
    val viewedAt = timestamp("viewed_at")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object SearchHistoriesTable : IntIdTable("search_histories") {
    val user = reference("user_id", UsersTable)
    val keyword = varchar("keyword", 100)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ChatRoomsTable : IntIdTable("chat_rooms") {
    val name = varchar("name", 100)
    val createdBy = reference("created_by", UsersTable)
    val isPrivate = bool("is_private")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ChatRoomMembersTable : IntIdTable("chat_room_members") {
    val room = reference("room_id", ChatRoomsTable)
    val user = reference("user_id", UsersTable)
    val joinedAt = timestamp("joined_at")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ChatMessagesTable : IntIdTable("chat_messages") {
    val room = reference("room_id", ChatRoomsTable)
    val sender = reference("sender_id", UsersTable)
    val message = text("message")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object PushSubscriptionsTable : IntIdTable("push_subscriptions") {
    val user = reference("user_id", UsersTable)
    val endpoint = varchar("endpoint", 1000).uniqueIndex()
    val p256dhKey = varchar("p256dh_key", 255)
    val authKey = varchar("auth_key", 255)
    val expirationTime = long("expiration_time").nullable()
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object PushPreferencesTable : IntIdTable("push_preferences") {
    val user = reference("user_id", UsersTable).uniqueIndex()
    val priceAlertEnabled = bool("price_alert_enabled")
    val orderStatusEnabled = bool("order_status_enabled")
    val chatMessageEnabled = bool("chat_message_enabled")
    val dealEnabled = bool("deal_enabled")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
