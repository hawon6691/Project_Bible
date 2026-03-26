package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

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

class ChatController(
    private val service: ChatService,
) {
    fun Route.register() {
        post("/chat/rooms") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createRoom(call.currentUserId(), call.receive()))
        }
        get("/chat/rooms") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(
                service.rooms(
                    userId = call.currentUserId(),
                    isAdmin = call.currentRole() == PbRole.ADMIN,
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/chat/rooms/{id}/messages") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(
                service.messages(
                    userId = call.currentUserId(),
                    isAdmin = call.currentRole() == PbRole.ADMIN,
                    roomId = call.roomId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        patch("/chat/rooms/{id}/close") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(
                service.closeRoom(
                    userId = call.currentUserId(),
                    isAdmin = call.currentRole() == PbRole.ADMIN,
                    roomId = call.roomId(),
                ),
            )
        }
    }

    private fun io.ktor.server.application.ApplicationCall.roomId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
