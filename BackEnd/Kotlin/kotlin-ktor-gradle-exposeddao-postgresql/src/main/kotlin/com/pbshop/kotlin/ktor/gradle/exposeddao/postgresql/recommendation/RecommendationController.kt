package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

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

class RecommendationController(
    private val service: RecommendationService,
) {
    fun Route.register() {
        get("/recommendations/today") {
            call.respondStub(service.today())
        }
        get("/recommendations/personalized") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.personalized(call.currentUserId(), call.request.queryParameters["limit"]?.toIntOrNull()))
        }
        get("/admin/recommendations") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.adminList())
        }
        post("/admin/recommendations") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        delete("/admin/recommendations/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.parameters["id"]?.toIntOrNull() ?: 0))
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
