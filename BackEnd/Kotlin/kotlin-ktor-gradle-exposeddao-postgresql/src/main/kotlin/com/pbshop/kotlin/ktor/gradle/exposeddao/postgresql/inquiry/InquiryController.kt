package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class InquiryController(
    private val service: InquiryService,
) {
    fun Route.register() {
        get("/products/{productId}/inquiries") {
            call.respondStub(
                service.productInquiries(
                    productId = call.productId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        post("/products/{productId}/inquiries") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createInquiry(call.currentUserId(), call.productId(), call.receive()))
        }
        post("/inquiries/{id}/answer") {
            call.requireAnyRole(PbRole.SELLER, PbRole.ADMIN)
            call.respondStub(service.answerInquiry(call.currentUserId(), call.inquiryId(), call.receive()))
        }
        get("/inquiries/me") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.myInquiries(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        delete("/inquiries/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.deleteInquiry(call.currentUserId(), call.inquiryId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.productId(): Int = parameters["productId"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.inquiryId(): Int = parameters["id"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
