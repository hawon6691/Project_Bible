package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class PaymentController(
    private val service: PaymentService,
) {
    fun Route.register() {
        post("/payments") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.create(call.currentUserId(), call.receive()))
        }
        get("/payments/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.detail(call.currentUserId(), call.paymentId()))
        }
        post("/payments/{id}/refund") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.refund(call.currentUserId(), call.paymentId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.paymentId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
