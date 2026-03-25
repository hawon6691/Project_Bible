package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FaqCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketStatus
import java.time.Instant

data class SupportTicketRecord(
    val id: Int,
    val ticketNumber: String,
    val userId: Int,
    val category: TicketCategory,
    val title: String,
    val content: String,
    val status: TicketStatus,
    val attachmentUrls: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val replies: List<TicketReplyRecord> = emptyList(),
)

data class TicketReplyRecord(
    val id: Int,
    val ticketId: Int,
    val userId: Int,
    val content: String,
    val isAdmin: Boolean,
    val createdAt: Instant,
)

data class SupportTicketListResult(
    val items: List<SupportTicketRecord>,
    val totalCount: Int,
)

data class NewSupportTicket(
    val category: TicketCategory,
    val title: String,
    val content: String,
    val attachmentUrls: List<String>,
)

data class FaqRecord(
    val id: Int,
    val category: FaqCategory,
    val question: String,
    val answer: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NewFaq(
    val category: FaqCategory,
    val question: String,
    val answer: String,
    val sortOrder: Int,
    val isActive: Boolean,
)

data class FaqUpdate(
    val category: FaqCategory?,
    val question: String?,
    val answer: String?,
    val sortOrder: Int?,
    val isActive: Boolean?,
)

data class NoticeRecord(
    val id: Int,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val viewCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NoticeListResult(
    val items: List<NoticeRecord>,
    val totalCount: Int,
)

data class NewNotice(
    val title: String,
    val content: String,
    val isPinned: Boolean,
)

data class NoticeUpdate(
    val title: String?,
    val content: String?,
    val isPinned: Boolean?,
)

interface SupportRepository {
    fun listUserTickets(
        userId: Int,
        page: Int,
        limit: Int,
        status: TicketStatus?,
        search: String?,
    ): SupportTicketListResult

    fun listAdminTickets(
        page: Int,
        limit: Int,
        status: TicketStatus?,
        category: TicketCategory?,
        search: String?,
    ): SupportTicketListResult

    fun findTicketById(ticketId: Int): SupportTicketRecord?

    fun createTicket(
        userId: Int,
        ticket: NewSupportTicket,
    ): SupportTicketRecord

    fun createReply(
        ticketId: Int,
        userId: Int,
        content: String,
        isAdmin: Boolean,
    ): TicketReplyRecord

    fun updateTicketStatus(
        ticketId: Int,
        status: TicketStatus,
    ): SupportTicketRecord

    fun listFaqs(
        category: FaqCategory?,
        search: String?,
    ): List<FaqRecord>

    fun createFaq(faq: NewFaq): FaqRecord

    fun updateFaq(
        faqId: Int,
        update: FaqUpdate,
    ): FaqRecord

    fun deleteFaq(faqId: Int)

    fun listNotices(
        page: Int,
        limit: Int,
    ): NoticeListResult

    fun findNoticeById(
        noticeId: Int,
        incrementViewCount: Boolean,
    ): NoticeRecord?

    fun createNotice(notice: NewNotice): NoticeRecord

    fun updateNotice(
        noticeId: Int,
        update: NoticeUpdate,
    ): NoticeRecord

    fun deleteNotice(noticeId: Int)
}
