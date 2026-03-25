package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FaqCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketStatus
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class InMemorySupportRepository private constructor(
    private val tickets: MutableList<SupportTicketRecord>,
    private val faqs: MutableList<FaqRecord>,
    private val notices: MutableList<NoticeRecord>,
) : SupportRepository {
    private var nextTicketId: Int = (tickets.maxOfOrNull { it.id } ?: 0) + 1
    private var nextReplyId: Int = (tickets.flatMap { it.replies }.maxOfOrNull { it.id } ?: 0) + 1
    private var nextFaqId: Int = (faqs.maxOfOrNull { it.id } ?: 0) + 1
    private var nextNoticeId: Int = (notices.maxOfOrNull { it.id } ?: 0) + 1

    override fun listUserTickets(
        userId: Int,
        page: Int,
        limit: Int,
        status: TicketStatus?,
        search: String?,
    ): SupportTicketListResult {
        val filtered =
            tickets
                .filter { it.userId == userId }
                .filter { status == null || it.status == status }
                .filter { search.isNullOrBlank() || it.title.contains(search, true) || it.content.contains(search, true) }
                .sortedByDescending { it.createdAt }
        return pageTickets(filtered, page, limit)
    }

    override fun listAdminTickets(
        page: Int,
        limit: Int,
        status: TicketStatus?,
        category: TicketCategory?,
        search: String?,
    ): SupportTicketListResult {
        val filtered =
            tickets
                .filter { status == null || it.status == status }
                .filter { category == null || it.category == category }
                .filter { search.isNullOrBlank() || it.title.contains(search, true) || it.content.contains(search, true) }
                .sortedByDescending { it.createdAt }
        return pageTickets(filtered, page, limit)
    }

    override fun findTicketById(ticketId: Int): SupportTicketRecord? = tickets.firstOrNull { it.id == ticketId }

    override fun createTicket(
        userId: Int,
        ticket: NewSupportTicket,
    ): SupportTicketRecord {
        val now = Instant.now()
        val created =
            SupportTicketRecord(
                id = nextTicketId++,
                ticketNumber = ticketNumber(now, nextTicketId - 1),
                userId = userId,
                category = ticket.category,
                title = ticket.title,
                content = ticket.content,
                status = TicketStatus.OPEN,
                attachmentUrls = ticket.attachmentUrls,
                createdAt = now,
                updatedAt = now,
            )
        tickets += created
        return created
    }

    override fun createReply(
        ticketId: Int,
        userId: Int,
        content: String,
        isAdmin: Boolean,
    ): TicketReplyRecord {
        val index = tickets.indexOfFirst { it.id == ticketId }
        check(index >= 0) { "Ticket $ticketId not found" }
        val reply =
            TicketReplyRecord(
                id = nextReplyId++,
                ticketId = ticketId,
                userId = userId,
                content = content,
                isAdmin = isAdmin,
                createdAt = Instant.now(),
            )
        tickets[index] =
            tickets[index].copy(
                replies = tickets[index].replies + reply,
                updatedAt = Instant.now(),
            )
        return reply
    }

    override fun updateTicketStatus(
        ticketId: Int,
        status: TicketStatus,
    ): SupportTicketRecord {
        val index = tickets.indexOfFirst { it.id == ticketId }
        check(index >= 0) { "Ticket $ticketId not found" }
        val updated = tickets[index].copy(status = status, updatedAt = Instant.now())
        tickets[index] = updated
        return updated
    }

    override fun listFaqs(
        category: FaqCategory?,
        search: String?,
    ): List<FaqRecord> =
        faqs
            .filter { category == null || it.category == category }
            .filter { search.isNullOrBlank() || it.question.contains(search, true) || it.answer.contains(search, true) }
            .sortedWith(compareBy<FaqRecord> { it.sortOrder }.thenBy { it.id })

    override fun createFaq(faq: NewFaq): FaqRecord {
        val now = Instant.now()
        val created =
            FaqRecord(
                id = nextFaqId++,
                category = faq.category,
                question = faq.question,
                answer = faq.answer,
                sortOrder = faq.sortOrder,
                isActive = faq.isActive,
                createdAt = now,
                updatedAt = now,
            )
        faqs += created
        return created
    }

    override fun updateFaq(
        faqId: Int,
        update: FaqUpdate,
    ): FaqRecord {
        val index = faqs.indexOfFirst { it.id == faqId }
        check(index >= 0) { "FAQ $faqId not found" }
        val current = faqs[index]
        val updated =
            current.copy(
                category = update.category ?: current.category,
                question = update.question ?: current.question,
                answer = update.answer ?: current.answer,
                sortOrder = update.sortOrder ?: current.sortOrder,
                isActive = update.isActive ?: current.isActive,
                updatedAt = Instant.now(),
            )
        faqs[index] = updated
        return updated
    }

    override fun deleteFaq(faqId: Int) {
        faqs.removeIf { it.id == faqId }
    }

    override fun listNotices(
        page: Int,
        limit: Int,
    ): NoticeListResult {
        val filtered = notices.sortedWith(compareByDescending<NoticeRecord> { it.isPinned }.thenByDescending { it.createdAt })
        val offset = (page - 1) * limit
        return NoticeListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findNoticeById(
        noticeId: Int,
        incrementViewCount: Boolean,
    ): NoticeRecord? {
        val index = notices.indexOfFirst { it.id == noticeId }
        if (index < 0) return null
        if (!incrementViewCount) return notices[index]
        val updated = notices[index].copy(viewCount = notices[index].viewCount + 1, updatedAt = Instant.now())
        notices[index] = updated
        return updated
    }

    override fun createNotice(notice: NewNotice): NoticeRecord {
        val now = Instant.now()
        val created =
            NoticeRecord(
                id = nextNoticeId++,
                title = notice.title,
                content = notice.content,
                isPinned = notice.isPinned,
                viewCount = 0,
                createdAt = now,
                updatedAt = now,
            )
        notices += created
        return created
    }

    override fun updateNotice(
        noticeId: Int,
        update: NoticeUpdate,
    ): NoticeRecord {
        val index = notices.indexOfFirst { it.id == noticeId }
        check(index >= 0) { "Notice $noticeId not found" }
        val current = notices[index]
        val updated =
            current.copy(
                title = update.title ?: current.title,
                content = update.content ?: current.content,
                isPinned = update.isPinned ?: current.isPinned,
                updatedAt = Instant.now(),
            )
        notices[index] = updated
        return updated
    }

    override fun deleteNotice(noticeId: Int) {
        notices.removeIf { it.id == noticeId }
    }

    private fun pageTickets(
        items: List<SupportTicketRecord>,
        page: Int,
        limit: Int,
    ): SupportTicketListResult {
        val offset = (page - 1) * limit
        return SupportTicketListResult(items.drop(offset).take(limit), items.size)
    }

    private fun ticketNumber(
        instant: Instant,
        sequence: Int,
    ): String =
        "TCK-${DateTimeFormatter.BASIC_ISO_DATE.format(instant.atZone(ZoneOffset.UTC).toLocalDate())}-${sequence.toString().padStart(4, '0')}"

    companion object {
        fun seeded(): InMemorySupportRepository {
            val createdAt = Instant.now().minusSeconds(172_800)
            val ticket =
                SupportTicketRecord(
                    id = 1,
                    ticketNumber = "TCK-20260225-0001",
                    userId = 4,
                    category = TicketCategory.DELIVERY,
                    title = "배송 지연 문의",
                    content = "주문한 상품 배송이 지연되고 있습니다.",
                    status = TicketStatus.IN_PROGRESS,
                    attachmentUrls = listOf("https://img.example.com/ticket1.png"),
                    createdAt = createdAt,
                    updatedAt = createdAt.plusSeconds(86_400),
                    replies =
                        listOf(
                            TicketReplyRecord(1, 1, 4, "확인 부탁드립니다.", false, createdAt.plusSeconds(600)),
                            TicketReplyRecord(2, 1, 1, "현재 물류센터 출고 대기 중이며 내일 출고 예정입니다.", true, createdAt.plusSeconds(1_200)),
                        ),
                )
            return InMemorySupportRepository(
                tickets = mutableListOf(ticket),
                faqs =
                    mutableListOf(
                        FaqRecord(1, FaqCategory.ORDER, "주문 취소는 어떻게 하나요?", "마이페이지 > 주문내역에서 취소 가능합니다.", 1, true, createdAt, createdAt),
                        FaqRecord(2, FaqCategory.PAYMENT, "환불은 언제 처리되나요?", "결제수단에 따라 1~5영업일 소요됩니다.", 2, true, createdAt, createdAt),
                    ),
                notices =
                    mutableListOf(
                        NoticeRecord(1, "[점검 안내] 2/28 새벽 시스템 점검", "2/28 02:00~04:00 서비스 점검 예정입니다.", true, 102, createdAt, createdAt),
                        NoticeRecord(2, "배송 정책 변경 안내", "무료배송 기준이 일부 변경됩니다.", false, 58, createdAt.plusSeconds(3_600), createdAt.plusSeconds(3_600)),
                    ),
            )
        }
    }
}
