package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class SupportController(
    private val service: SupportService,
) {
    fun Route.register() {
        get("/support/tickets") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.tickets(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    status = call.request.queryParameters["status"],
                    search = call.request.queryParameters["search"],
                ),
            )
        }
        post("/support/tickets") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createTicket(call.currentUserId(), call.receiveTicketCreateRequest()))
        }
        get("/support/tickets/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val role = call.currentRole()
            call.respondStub(service.detail(call.currentUserId(), role == PbRole.ADMIN, call.ticketId()))
        }
        post("/support/tickets/{id}/reply") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val role = call.currentRole()
            call.respondStub(service.createReply(call.currentUserId(), role == PbRole.ADMIN, call.ticketId(), call.receive()))
        }
        get("/admin/support/tickets") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.adminTickets(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    status = call.request.queryParameters["status"],
                    category = call.request.queryParameters["category"],
                    search = call.request.queryParameters["search"],
                ),
            )
        }
        patch("/admin/support/tickets/{id}/status") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateStatus(call.ticketId(), call.receive()))
        }
        get("/faqs") {
            call.respondStub(service.faqs(call.request.queryParameters["category"], call.request.queryParameters["search"]))
        }
        post("/faqs") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.createFaq(call.receive()))
        }
        patch("/faqs/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateFaq(call.faqId(), call.receive()))
        }
        delete("/faqs/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteFaq(call.faqId()))
        }
        get("/notices") {
            call.respondStub(
                service.notices(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/notices/{id}") {
            call.respondStub(service.noticeDetail(call.noticeId()))
        }
        post("/notices") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.createNotice(call.receive()))
        }
        patch("/notices/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateNotice(call.noticeId(), call.receive()))
        }
        delete("/notices/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteNotice(call.noticeId()))
        }
    }

    private suspend fun io.ktor.server.application.ApplicationCall.receiveTicketCreateRequest(): SupportTicketCreateRequest {
        val multipart = receiveMultipart()
        var category: String? = null
        var title: String? = null
        var content: String? = null
        val attachments = mutableListOf<String>()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "category" -> category = part.value
                        "title" -> title = part.value
                        "content" -> content = part.value
                    }
                }
                is PartData.FileItem -> attachments += (part.originalFileName ?: part.name ?: "support-attachment.bin")
                else -> Unit
            }
            part.dispose()
        }
        return SupportTicketCreateRequest(
            category =
                category
                    ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "category는 필수입니다."),
            title =
                title
                    ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title은 필수입니다."),
            content =
                content
                    ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 필수입니다."),
            attachments = attachments,
        )
    }

    private fun io.ktor.server.application.ApplicationCall.ticketId(): Int = parameters["id"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.faqId(): Int = parameters["id"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.noticeId(): Int = parameters["id"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
