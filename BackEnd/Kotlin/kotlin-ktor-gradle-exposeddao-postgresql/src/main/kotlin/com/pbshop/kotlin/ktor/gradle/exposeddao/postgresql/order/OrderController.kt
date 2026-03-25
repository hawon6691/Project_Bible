package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

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
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class OrderController(
    private val service: OrderService,
) {
    fun Route.register() {
        post("/orders") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.create(call.currentUserId(), call.receive()))
        }
        get("/orders") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.list(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    status = call.request.queryParameters["status"],
                ),
            )
        }
        get("/orders/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.detail(call.currentUserId(), call.orderId()))
        }
        post("/orders/{id}/cancel") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.cancel(call.currentUserId(), call.orderId()))
        }
        get("/admin/orders") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.adminList(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    status = call.request.queryParameters["status"],
                ),
            )
        }
        patch("/admin/orders/{id}/status") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.adminUpdateStatus(call.orderId(), call.receive()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.orderId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
