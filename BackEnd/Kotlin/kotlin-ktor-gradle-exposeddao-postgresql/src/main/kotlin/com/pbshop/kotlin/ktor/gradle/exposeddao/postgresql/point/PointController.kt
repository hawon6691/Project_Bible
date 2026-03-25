package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

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

class PointController(
    private val service: PointService,
) {
    fun Route.register() {
        get("/points/balance") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.balance(call.currentUserId()))
        }
        get("/points/transactions") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.transactions(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    type = call.request.queryParameters["type"],
                ),
            )
        }
        post("/admin/points/grant") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.grant(call.receive()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
