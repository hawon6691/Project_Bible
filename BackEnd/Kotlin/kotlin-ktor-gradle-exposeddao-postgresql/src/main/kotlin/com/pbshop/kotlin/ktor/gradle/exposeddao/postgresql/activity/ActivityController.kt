package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get

class ActivityController(
    private val service: ActivityService,
) {
    fun Route.register() {
        get("/activity/views") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.views(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        delete("/activity/views") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.clearViews(call.currentUserId()))
        }
        get("/activity/searches") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.searches(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        delete("/activity/searches") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.clearSearches(call.currentUserId()))
        }
        delete("/activity/searches/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.deleteSearch(call.currentUserId(), call.searchId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.searchId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
