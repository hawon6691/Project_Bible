package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object PcBuildsTable : IntIdTable("pc_builds") {
    val user = reference("user_id", UsersTable)
    val name = varchar("name", 120)
    val description = varchar("description", 500).nullable()
    val purpose = varchar("purpose", 30)
    val budget = integer("budget").nullable()
    val totalPrice = integer("total_price")
    val shareCode = varchar("share_code", 40).nullable()
    val viewCount = integer("view_count")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class PcBuildEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PcBuildEntity>(PcBuildsTable)

    var userId by PcBuildsTable.user
    var name by PcBuildsTable.name
    var description by PcBuildsTable.description
    var purpose by PcBuildsTable.purpose
    var budget by PcBuildsTable.budget
    var totalPrice by PcBuildsTable.totalPrice
    var shareCode by PcBuildsTable.shareCode
    var viewCount by PcBuildsTable.viewCount
    var createdAt by PcBuildsTable.createdAt
    var updatedAt by PcBuildsTable.updatedAt
    var deletedAt by PcBuildsTable.deletedAt
}

object PcBuildPartsTable : IntIdTable("pc_build_parts") {
    val build = reference("build_id", PcBuildsTable)
    val product = reference("product_id", ProductsTable)
    val seller = reference("seller_id", SellersTable)
    val partType = varchar("part_type", 20)
    val quantity = integer("quantity")
    val unitPrice = integer("unit_price")
    val totalPrice = integer("total_price")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object PcCompatibilityRulesTable : IntIdTable("pc_compatibility_rules") {
    val partType = varchar("part_type", 20)
    val targetPartType = varchar("target_part_type", 20).nullable()
    val title = varchar("title", 100)
    val description = varchar("description", 500)
    val severity = varchar("severity", 10)
    val enabled = bool("enabled")
    val metadata = text("metadata").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object FriendshipsTable : IntIdTable("friendships") {
    val requester = reference("requester_id", UsersTable)
    val addressee = reference("addressee_id", UsersTable)
    val status = varchar("status", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object FriendBlocksTable : IntIdTable("friend_blocks") {
    val user = reference("user_id", UsersTable)
    val blockedUser = reference("blocked_user_id", UsersTable)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object FriendActivitiesTable : IntIdTable("friend_activities") {
    val user = reference("user_id", UsersTable)
    val type = varchar("type", 40)
    val message = varchar("message", 300)
    val metadata = text("metadata").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ShortformsTable : IntIdTable("shortforms") {
    val user = reference("user_id", UsersTable)
    val title = varchar("title", 120)
    val videoUrl = varchar("video_url", 500)
    val thumbnailUrl = varchar("thumbnail_url", 500).nullable()
    val durationSec = integer("duration_sec")
    val viewCount = integer("view_count")
    val likeCount = integer("like_count")
    val commentCount = integer("comment_count")
    val transcodeStatus = varchar("transcode_status", 20)
    val transcodedVideoUrl = varchar("transcoded_video_url", 500).nullable()
    val transcodeError = varchar("transcode_error", 500).nullable()
    val transcodedAt = timestamp("transcoded_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ShortformProductsTable : IntIdTable("shortform_products") {
    val shortform = reference("shortform_id", ShortformsTable)
    val product = reference("product_id", ProductsTable)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ShortformLikesTable : IntIdTable("shortform_likes") {
    val shortform = reference("shortform_id", ShortformsTable)
    val user = reference("user_id", UsersTable)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object ShortformCommentsTable : IntIdTable("shortform_comments") {
    val shortform = reference("shortform_id", ShortformsTable)
    val user = reference("user_id", UsersTable)
    val content = varchar("content", 500)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
