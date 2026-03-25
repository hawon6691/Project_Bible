package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FaqCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FaqEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FaqsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.NoticeEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.NoticesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SupportTicketEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SupportTicketsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketRepliesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketReplyEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ExposedDaoSupportRepository(
    private val databaseFactory: DatabaseFactory,
) : SupportRepository {
    override fun listUserTickets(
        userId: Int,
        page: Int,
        limit: Int,
        status: TicketStatus?,
        search: String?,
    ): SupportTicketListResult =
        databaseFactory.withTransaction {
            val rows =
                SupportTicketsTable.selectAll()
                    .where { SupportTicketsTable.user eq userId }
                    .map(::toTicketRecord)
                    .filter { status == null || it.status == status }
                    .filter { search.isNullOrBlank() || it.title.contains(search, true) || it.content.contains(search, true) }
                    .sortedByDescending { it.createdAt }
            val offset = (page - 1) * limit
            SupportTicketListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun listAdminTickets(
        page: Int,
        limit: Int,
        status: TicketStatus?,
        category: TicketCategory?,
        search: String?,
    ): SupportTicketListResult =
        databaseFactory.withTransaction {
            val rows =
                SupportTicketsTable.selectAll()
                    .map(::toTicketRecord)
                    .filter { status == null || it.status == status }
                    .filter { category == null || it.category == category }
                    .filter { search.isNullOrBlank() || it.title.contains(search, true) || it.content.contains(search, true) }
                    .sortedByDescending { it.createdAt }
            val offset = (page - 1) * limit
            SupportTicketListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findTicketById(ticketId: Int): SupportTicketRecord? =
        databaseFactory.withTransaction {
            SupportTicketsTable.selectAll()
                .where { SupportTicketsTable.id eq ticketId }
                .limit(1)
                .firstOrNull()
                ?.let { row ->
                    toTicketRecord(row).copy(replies = listReplies(ticketId))
                }
        }

    override fun createTicket(
        userId: Int,
        ticket: NewSupportTicket,
    ): SupportTicketRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val id = (SupportTicketsTable.selectAll().maxOfOrNull { it[SupportTicketsTable.id].value } ?: 0) + 1
            val entity =
                SupportTicketEntity.new {
                    ticketNumber = ticketNumber(now, id)
                    this.userId = EntityID(userId, UsersTable)
                    category = ticket.category
                    title = ticket.title
                    content = ticket.content
                    status = TicketStatus.OPEN
                    attachmentUrls = encodeUrls(ticket.attachmentUrls)
                    createdAt = now
                    updatedAt = now
                }
            toTicketRecord(
                SupportTicketsTable.selectAll()
                    .where { SupportTicketsTable.id eq entity.id.value }
                    .single(),
            )
        }

    override fun createReply(
        ticketId: Int,
        userId: Int,
        content: String,
        isAdmin: Boolean,
    ): TicketReplyRecord =
        databaseFactory.withTransaction {
            val reply =
                TicketReplyEntity.new {
                    this.ticketId = EntityID(ticketId, SupportTicketsTable)
                    this.userId = EntityID(userId, UsersTable)
                    this.content = content
                    this.isAdmin = isAdmin
                    createdAt = Instant.now()
                }
            SupportTicketEntity.findById(ticketId)?.updatedAt = Instant.now()
            toReplyRecord(
                TicketRepliesTable.selectAll()
                    .where { TicketRepliesTable.id eq reply.id.value }
                    .single(),
            )
        }

    override fun updateTicketStatus(
        ticketId: Int,
        status: TicketStatus,
    ): SupportTicketRecord =
        databaseFactory.withTransaction {
            val entity = requireNotNull(SupportTicketEntity.findById(ticketId)) { "Ticket $ticketId not found" }
            entity.status = status
            entity.updatedAt = Instant.now()
            findTicketById(ticketId) ?: error("Ticket $ticketId not found after update")
        }

    override fun listFaqs(
        category: FaqCategory?,
        search: String?,
    ): List<FaqRecord> =
        databaseFactory.withTransaction {
            FaqsTable.selectAll()
                .map(::toFaqRecord)
                .filter { category == null || it.category == category }
                .filter { search.isNullOrBlank() || it.question.contains(search, true) || it.answer.contains(search, true) }
                .sortedWith(compareBy<FaqRecord> { it.sortOrder }.thenBy { it.id })
        }

    override fun createFaq(faq: NewFaq): FaqRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val entity =
                FaqEntity.new {
                    category = faq.category
                    question = faq.question
                    answer = faq.answer
                    sortOrder = faq.sortOrder
                    isActive = faq.isActive
                    createdAt = now
                    updatedAt = now
                }
            toFaqRecord(
                FaqsTable.selectAll()
                    .where { FaqsTable.id eq entity.id.value }
                    .single(),
            )
        }

    override fun updateFaq(
        faqId: Int,
        update: FaqUpdate,
    ): FaqRecord =
        databaseFactory.withTransaction {
            val entity = requireNotNull(FaqEntity.findById(faqId)) { "FAQ $faqId not found" }
            update.category?.let { entity.category = it }
            update.question?.let { entity.question = it }
            update.answer?.let { entity.answer = it }
            update.sortOrder?.let { entity.sortOrder = it }
            update.isActive?.let { entity.isActive = it }
            entity.updatedAt = Instant.now()
            toFaqRecord(
                FaqsTable.selectAll()
                    .where { FaqsTable.id eq faqId }
                    .single(),
            )
        }

    override fun deleteFaq(faqId: Int) {
        databaseFactory.withTransaction {
            FaqEntity.findById(faqId)?.delete()
        }
    }

    override fun listNotices(
        page: Int,
        limit: Int,
    ): NoticeListResult =
        databaseFactory.withTransaction {
            val rows =
                NoticesTable.selectAll()
                    .orderBy(NoticesTable.isPinned to SortOrder.DESC, NoticesTable.createdAt to SortOrder.DESC)
                    .map(::toNoticeRecord)
            val offset = (page - 1) * limit
            NoticeListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findNoticeById(
        noticeId: Int,
        incrementViewCount: Boolean,
    ): NoticeRecord? =
        databaseFactory.withTransaction {
            val entity = NoticeEntity.findById(noticeId) ?: return@withTransaction null
            if (incrementViewCount) {
                entity.viewCount += 1
                entity.updatedAt = Instant.now()
            }
            toNoticeRecord(
                NoticesTable.selectAll()
                    .where { NoticesTable.id eq noticeId }
                    .single(),
            )
        }

    override fun createNotice(notice: NewNotice): NoticeRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val entity =
                NoticeEntity.new {
                    title = notice.title
                    content = notice.content
                    isPinned = notice.isPinned
                    viewCount = 0
                    createdAt = now
                    updatedAt = now
                }
            toNoticeRecord(
                NoticesTable.selectAll()
                    .where { NoticesTable.id eq entity.id.value }
                    .single(),
            )
        }

    override fun updateNotice(
        noticeId: Int,
        update: NoticeUpdate,
    ): NoticeRecord =
        databaseFactory.withTransaction {
            val entity = requireNotNull(NoticeEntity.findById(noticeId)) { "Notice $noticeId not found" }
            update.title?.let { entity.title = it }
            update.content?.let { entity.content = it }
            update.isPinned?.let { entity.isPinned = it }
            entity.updatedAt = Instant.now()
            toNoticeRecord(
                NoticesTable.selectAll()
                    .where { NoticesTable.id eq noticeId }
                    .single(),
            )
        }

    override fun deleteNotice(noticeId: Int) {
        databaseFactory.withTransaction {
            NoticeEntity.findById(noticeId)?.delete()
        }
    }

    private fun listReplies(ticketId: Int): List<TicketReplyRecord> =
        TicketRepliesTable.selectAll()
            .where { TicketRepliesTable.ticket eq ticketId }
            .orderBy(TicketRepliesTable.createdAt to SortOrder.ASC, TicketRepliesTable.id to SortOrder.ASC)
            .map(::toReplyRecord)

    private fun toTicketRecord(row: org.jetbrains.exposed.sql.ResultRow): SupportTicketRecord =
        SupportTicketRecord(
            id = row[SupportTicketsTable.id].value,
            ticketNumber = row[SupportTicketsTable.ticketNumber],
            userId = row[SupportTicketsTable.user].value,
            category = row[SupportTicketsTable.category],
            title = row[SupportTicketsTable.title],
            content = row[SupportTicketsTable.content],
            status = row[SupportTicketsTable.status],
            attachmentUrls = decodeUrls(row[SupportTicketsTable.attachmentUrls]),
            createdAt = row[SupportTicketsTable.createdAt],
            updatedAt = row[SupportTicketsTable.updatedAt],
        )

    private fun toReplyRecord(row: org.jetbrains.exposed.sql.ResultRow): TicketReplyRecord =
        TicketReplyRecord(
            id = row[TicketRepliesTable.id].value,
            ticketId = row[TicketRepliesTable.ticket].value,
            userId = row[TicketRepliesTable.user].value,
            content = row[TicketRepliesTable.content],
            isAdmin = row[TicketRepliesTable.isAdmin],
            createdAt = row[TicketRepliesTable.createdAt],
        )

    private fun toFaqRecord(row: org.jetbrains.exposed.sql.ResultRow): FaqRecord =
        FaqRecord(
            id = row[FaqsTable.id].value,
            category = row[FaqsTable.category],
            question = row[FaqsTable.question],
            answer = row[FaqsTable.answer],
            sortOrder = row[FaqsTable.sortOrder],
            isActive = row[FaqsTable.isActive],
            createdAt = row[FaqsTable.createdAt],
            updatedAt = row[FaqsTable.updatedAt],
        )

    private fun toNoticeRecord(row: org.jetbrains.exposed.sql.ResultRow): NoticeRecord =
        NoticeRecord(
            id = row[NoticesTable.id].value,
            title = row[NoticesTable.title],
            content = row[NoticesTable.content],
            isPinned = row[NoticesTable.isPinned],
            viewCount = row[NoticesTable.viewCount],
            createdAt = row[NoticesTable.createdAt],
            updatedAt = row[NoticesTable.updatedAt],
        )

    private fun ticketNumber(
        instant: Instant,
        sequence: Int,
    ): String =
        "TCK-${DateTimeFormatter.BASIC_ISO_DATE.format(instant.atZone(ZoneOffset.UTC).toLocalDate())}-${sequence.toString().padStart(4, '0')}"

    private fun encodeUrls(urls: List<String>): String? =
        urls.takeIf { it.isNotEmpty() }?.let { Json.encodeToString(ListSerializer(String.serializer()), it) }

    private fun decodeUrls(raw: String?): List<String> =
        raw?.takeIf { it.isNotBlank() }?.let { Json.decodeFromString(ListSerializer(String.serializer()), it) } ?: emptyList()
}
