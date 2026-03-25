package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.FaqCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketCategory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TicketStatus
import io.ktor.http.HttpStatusCode

class SupportService(
    private val repository: SupportRepository,
) {
    fun tickets(
        userId: Int,
        page: Int,
        limit: Int,
        status: String?,
        search: String?,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listUserTickets(userId, queryPage, queryLimit, status?.let(::parseTicketStatus), search?.trim())
        return ticketPage(result, queryPage, queryLimit)
    }

    fun createTicket(
        userId: Int,
        request: SupportTicketCreateRequest,
    ): StubResponse {
        if (request.title.trim().isBlank() || request.content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title과 content는 비어 있을 수 없습니다.")
        }
        if (request.attachments.size > 3) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "attachments는 최대 3개까지 업로드할 수 있습니다.")
        }
        val created =
            repository.createTicket(
                userId,
                NewSupportTicket(
                    category = parseTicketCategory(request.category),
                    title = request.title.trim(),
                    content = request.content.trim(),
                    attachmentUrls = request.attachments.map(::attachmentUrl),
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = ticketPayload(created))
    }

    fun detail(
        userId: Int,
        isAdmin: Boolean,
        ticketId: Int,
    ): StubResponse {
        val ticket = requireTicket(ticketId)
        if (!isAdmin && ticket.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "TICKET_FORBIDDEN", "본인 문의만 조회할 수 있습니다.")
        }
        return StubResponse(data = ticketDetailPayload(ticket))
    }

    fun createReply(
        userId: Int,
        isAdmin: Boolean,
        ticketId: Int,
        request: SupportReplyRequest,
    ): StubResponse {
        if (request.content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.")
        }
        val ticket = requireTicket(ticketId)
        if (!isAdmin && ticket.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "TICKET_FORBIDDEN", "본인 문의에만 답글을 작성할 수 있습니다.")
        }
        val reply = repository.createReply(ticketId, userId, request.content.trim(), isAdmin)
        return StubResponse(status = HttpStatusCode.Created, data = replyPayload(reply))
    }

    fun adminTickets(
        page: Int,
        limit: Int,
        status: String?,
        category: String?,
        search: String?,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result =
            repository.listAdminTickets(
                page = queryPage,
                limit = queryLimit,
                status = status?.let(::parseTicketStatus),
                category = category?.let(::parseTicketCategory),
                search = search?.trim(),
            )
        return ticketPage(result, queryPage, queryLimit)
    }

    fun updateStatus(
        ticketId: Int,
        request: SupportStatusUpdateRequest,
    ): StubResponse =
        StubResponse(data = ticketPayload(repository.updateTicketStatus(ticketId, parseTicketStatus(request.status))))

    fun faqs(
        category: String?,
        search: String?,
    ): StubResponse =
        StubResponse(data = repository.listFaqs(category?.let(::parseFaqCategory), search?.trim()).map(::faqPayload))

    fun createFaq(request: FaqRequest): StubResponse {
        validateFaq(request.question, request.answer)
        val created =
            repository.createFaq(
                NewFaq(
                    category = parseFaqCategory(request.category),
                    question = request.question.trim(),
                    answer = request.answer.trim(),
                    sortOrder = request.sortOrder,
                    isActive = request.isActive,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = faqPayload(created))
    }

    fun updateFaq(
        faqId: Int,
        request: FaqUpdateRequest,
    ): StubResponse {
        request.question?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "question은 비어 있을 수 없습니다.") }
        request.answer?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "answer는 비어 있을 수 없습니다.") }
        val updated =
            repository.updateFaq(
                faqId,
                FaqUpdate(
                    category = request.category?.let(::parseFaqCategory),
                    question = request.question?.trim(),
                    answer = request.answer?.trim(),
                    sortOrder = request.sortOrder,
                    isActive = request.isActive,
                ),
            )
        return StubResponse(data = faqPayload(updated))
    }

    fun deleteFaq(faqId: Int): StubResponse {
        repository.deleteFaq(faqId)
        return StubResponse(data = mapOf("message" to "FAQ deleted."))
    }

    fun notices(
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listNotices(queryPage, queryLimit)
        return StubResponse(
            data = result.items.map(::noticePayload),
            meta =
                mapOf(
                    "page" to queryPage,
                    "limit" to queryLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + queryLimit - 1) / queryLimit),
                ),
        )
    }

    fun noticeDetail(noticeId: Int): StubResponse =
        StubResponse(
            data =
                noticePayload(
                    repository.findNoticeById(noticeId, incrementViewCount = true)
                        ?: throw PbShopException(HttpStatusCode.NotFound, "NOTICE_NOT_FOUND", "공지사항을 찾을 수 없습니다."),
                ),
        )

    fun createNotice(request: NoticeRequest): StubResponse {
        validateNotice(request.title, request.content)
        val created = repository.createNotice(NewNotice(request.title.trim(), request.content.trim(), request.isPinned))
        return StubResponse(status = HttpStatusCode.Created, data = noticePayload(created))
    }

    fun updateNotice(
        noticeId: Int,
        request: NoticeUpdateRequest,
    ): StubResponse {
        request.title?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title은 비어 있을 수 없습니다.") }
        request.content?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.") }
        val updated =
            repository.updateNotice(
                noticeId,
                NoticeUpdate(
                    title = request.title?.trim(),
                    content = request.content?.trim(),
                    isPinned = request.isPinned,
                ),
            )
        return StubResponse(data = noticePayload(updated))
    }

    fun deleteNotice(noticeId: Int): StubResponse {
        repository.deleteNotice(noticeId)
        return StubResponse(data = mapOf("message" to "Notice deleted."))
    }

    private fun requireTicket(ticketId: Int): SupportTicketRecord =
        repository.findTicketById(ticketId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "TICKET_NOT_FOUND", "문의 티켓을 찾을 수 없습니다.")

    private fun validateFaq(
        question: String,
        answer: String,
    ) {
        if (question.trim().isBlank() || answer.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "question과 answer는 비어 있을 수 없습니다.")
        }
    }

    private fun validateNotice(
        title: String,
        content: String,
    ) {
        if (title.trim().isBlank() || content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title과 content는 비어 있을 수 없습니다.")
        }
    }

    private fun ticketPage(
        result: SupportTicketListResult,
        page: Int,
        limit: Int,
    ): StubResponse =
        StubResponse(
            data = result.items.map(::ticketPayload),
            meta =
                mapOf(
                    "page" to page,
                    "limit" to limit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + limit - 1) / limit),
                ),
        )

    private fun ticketPayload(record: SupportTicketRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "ticketNumber" to record.ticketNumber,
            "userId" to record.userId,
            "category" to record.category.name,
            "title" to record.title,
            "content" to record.content,
            "status" to record.status.name,
            "attachmentUrls" to record.attachmentUrls,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun ticketDetailPayload(record: SupportTicketRecord): Map<String, Any?> =
        ticketPayload(record) + ("replies" to record.replies.map(::replyPayload))

    private fun replyPayload(record: TicketReplyRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "ticketId" to record.ticketId,
            "userId" to record.userId,
            "content" to record.content,
            "isAdmin" to record.isAdmin,
            "createdAt" to record.createdAt.toString(),
        )

    private fun faqPayload(record: FaqRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "category" to record.category.name,
            "question" to record.question,
            "answer" to record.answer,
            "sortOrder" to record.sortOrder,
            "isActive" to record.isActive,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun noticePayload(record: NoticeRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "title" to record.title,
            "content" to record.content,
            "isPinned" to record.isPinned,
            "viewCount" to record.viewCount,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun attachmentUrl(fileName: String): String = "https://img.example.com/support/${fileName.trim()}"

    private fun parseTicketCategory(value: String): TicketCategory =
        runCatching { TicketCategory.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 ticket category입니다.")
            }

    private fun parseTicketStatus(value: String): TicketStatus =
        runCatching { TicketStatus.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 ticket status입니다.")
            }

    private fun parseFaqCategory(value: String): FaqCategory =
        runCatching { FaqCategory.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 FAQ category입니다.")
            }
}
