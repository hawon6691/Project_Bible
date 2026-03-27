package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

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
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class TrustController(
    private val service: TrustService,
) {
    fun Route.register() {
        get("/sellers/{id}/trust") {
            call.respondStub(service.detail(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        get("/sellers/{id}/reviews") {
            call.respondStub(
                service.reviews(
                    sellerId = call.parameters["id"]?.toIntOrNull() ?: 0,
                    page = call.request.queryParameters["page"]?.toIntOrNull(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                    sort = call.request.queryParameters["sort"],
                ),
            )
        }
        post("/sellers/{id}/reviews") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.create(call.parameters["id"]?.toIntOrNull() ?: 0, call.currentUserId(), call.receive()))
        }
        patch("/seller-reviews/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(
                service.update(
                    reviewId = call.parameters["id"]?.toIntOrNull() ?: 0,
                    userId = call.currentUserId(),
                    isAdmin = call.currentRole() == PbRole.ADMIN,
                    request = call.receive(),
                ),
            )
        }
        delete("/seller-reviews/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(
                service.delete(
                    reviewId = call.parameters["id"]?.toIntOrNull() ?: 0,
                    userId = call.currentUserId(),
                    isAdmin = call.currentRole() == PbRole.ADMIN,
                ),
            )
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
