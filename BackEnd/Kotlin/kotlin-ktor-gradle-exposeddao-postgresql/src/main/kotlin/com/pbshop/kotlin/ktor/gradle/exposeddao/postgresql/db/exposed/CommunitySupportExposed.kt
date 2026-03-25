package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

enum class TicketCategory {
    ORDER,
    PAYMENT,
    DELIVERY,
    ACCOUNT,
    OTHER,
}

enum class TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
}

enum class FaqCategory {
    GENERAL,
    ORDER,
    PAYMENT,
    DELIVERY,
    ACCOUNT,
}

object BoardsTable : IntIdTable("boards") {
    val name = varchar("name", 50)
    val slug = varchar("slug", 50)
    val description = varchar("description", 200).nullable()
    val sortOrder = integer("sort_order")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
}

class BoardEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BoardEntity>(BoardsTable)

    var name by BoardsTable.name
    var slug by BoardsTable.slug
    var description by BoardsTable.description
    var sortOrder by BoardsTable.sortOrder
    var isActive by BoardsTable.isActive
    var createdAt by BoardsTable.createdAt
}

object PostsTable : IntIdTable("posts") {
    val board = reference("board_id", BoardsTable)
    val user = reference("user_id", UsersTable)
    val title = varchar("title", 200)
    val content = text("content")
    val viewCount = integer("view_count")
    val likeCount = integer("like_count")
    val commentCount = integer("comment_count")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class PostEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostEntity>(PostsTable)

    var boardId by PostsTable.board
    var userId by PostsTable.user
    var title by PostsTable.title
    var content by PostsTable.content
    var viewCount by PostsTable.viewCount
    var likeCount by PostsTable.likeCount
    var commentCount by PostsTable.commentCount
    var createdAt by PostsTable.createdAt
    var updatedAt by PostsTable.updatedAt
    var deletedAt by PostsTable.deletedAt
}

object CommentsTable : IntIdTable("comments") {
    val post = reference("post_id", PostsTable)
    val user = reference("user_id", UsersTable)
    val parent = reference("parent_id", CommentsTable).nullable()
    val content = text("content")
    val createdAt = timestamp("created_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class CommentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CommentEntity>(CommentsTable)

    var postId by CommentsTable.post
    var userId by CommentsTable.user
    var parentId by CommentsTable.parent
    var content by CommentsTable.content
    var createdAt by CommentsTable.createdAt
    var deletedAt by CommentsTable.deletedAt
}

object PostLikesTable : IntIdTable("post_likes") {
    val post = reference("post_id", PostsTable)
    val user = reference("user_id", UsersTable)
    val createdAt = timestamp("created_at")
}

class PostLikeEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostLikeEntity>(PostLikesTable)

    var postId by PostLikesTable.post
    var userId by PostLikesTable.user
    var createdAt by PostLikesTable.createdAt
}

object InquiriesTable : IntIdTable("inquiries") {
    val product = reference("product_id", ProductsTable)
    val user = reference("user_id", UsersTable)
    val title = varchar("title", 200)
    val content = text("content")
    val isSecret = bool("is_secret")
    val answer = text("answer").nullable()
    val answeredBy = reference("answered_by", UsersTable).nullable()
    val answeredAt = timestamp("answered_at").nullable()
    val createdAt = timestamp("created_at")
}

class InquiryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<InquiryEntity>(InquiriesTable)

    var productId by InquiriesTable.product
    var userId by InquiriesTable.user
    var title by InquiriesTable.title
    var content by InquiriesTable.content
    var isSecret by InquiriesTable.isSecret
    var answer by InquiriesTable.answer
    var answeredBy by InquiriesTable.answeredBy
    var answeredAt by InquiriesTable.answeredAt
    var createdAt by InquiriesTable.createdAt
}

object SupportTicketsTable : IntIdTable("support_tickets") {
    val ticketNumber = varchar("ticket_number", 30)
    val user = reference("user_id", UsersTable)
    val category = pgEnum<TicketCategory>("category", "ticket_category")
    val title = varchar("title", 200)
    val content = text("content")
    val status = pgEnum<TicketStatus>("status", "ticket_status")
    val attachmentUrls = text("attachment_urls").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class SupportTicketEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SupportTicketEntity>(SupportTicketsTable)

    var ticketNumber by SupportTicketsTable.ticketNumber
    var userId by SupportTicketsTable.user
    var category by SupportTicketsTable.category
    var title by SupportTicketsTable.title
    var content by SupportTicketsTable.content
    var status by SupportTicketsTable.status
    var attachmentUrls by SupportTicketsTable.attachmentUrls
    var createdAt by SupportTicketsTable.createdAt
    var updatedAt by SupportTicketsTable.updatedAt
}

object TicketRepliesTable : IntIdTable("ticket_replies") {
    val ticket = reference("ticket_id", SupportTicketsTable)
    val user = reference("user_id", UsersTable)
    val content = text("content")
    val isAdmin = bool("is_admin")
    val createdAt = timestamp("created_at")
}

class TicketReplyEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TicketReplyEntity>(TicketRepliesTable)

    var ticketId by TicketRepliesTable.ticket
    var userId by TicketRepliesTable.user
    var content by TicketRepliesTable.content
    var isAdmin by TicketRepliesTable.isAdmin
    var createdAt by TicketRepliesTable.createdAt
}

object FaqsTable : IntIdTable("faqs") {
    val category = pgEnum<FaqCategory>("category", "faq_category")
    val question = varchar("question", 300)
    val answer = text("answer")
    val sortOrder = integer("sort_order")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class FaqEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FaqEntity>(FaqsTable)

    var category by FaqsTable.category
    var question by FaqsTable.question
    var answer by FaqsTable.answer
    var sortOrder by FaqsTable.sortOrder
    var isActive by FaqsTable.isActive
    var createdAt by FaqsTable.createdAt
    var updatedAt by FaqsTable.updatedAt
}

object NoticesTable : IntIdTable("notices") {
    val title = varchar("title", 200)
    val content = text("content")
    val isPinned = bool("is_pinned")
    val viewCount = integer("view_count")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class NoticeEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NoticeEntity>(NoticesTable)

    var title by NoticesTable.title
    var content by NoticesTable.content
    var isPinned by NoticesTable.isPinned
    var viewCount by NoticesTable.viewCount
    var createdAt by NoticesTable.createdAt
    var updatedAt by NoticesTable.updatedAt
}
