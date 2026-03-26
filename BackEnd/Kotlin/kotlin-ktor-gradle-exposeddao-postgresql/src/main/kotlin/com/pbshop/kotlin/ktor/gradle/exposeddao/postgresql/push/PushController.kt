package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

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

class PushController(
    private val service: PushService,
) {
    fun Route.register() {
        post("/push/subscriptions") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.subscribe(call.currentUserId(), call.receive()))
        }
        post("/push/subscriptions/unsubscribe") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.unsubscribe(call.currentUserId(), call.receive()))
        }
        get("/push/subscriptions") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.subscriptions(call.currentUserId()))
        }
        get("/push/preferences") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.preferences(call.currentUserId()))
        }
        post("/push/preferences") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.updatePreferences(call.currentUserId(), call.receive()))
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
