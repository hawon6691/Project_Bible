package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class FriendController(
    private val service: FriendService,
) {
    fun Route.register() {
        post("/friends/request/{userId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.requestFriend(call.currentUserId(), call.pathInt("userId")))
        }
        patch("/friends/request/{friendshipId}/accept") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.acceptRequest(call.currentUserId(), call.pathInt("friendshipId")))
        }
        patch("/friends/request/{friendshipId}/reject") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.rejectRequest(call.currentUserId(), call.pathInt("friendshipId")))
        }
        get("/friends") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.listFriends(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/friends/requests/received") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.receivedRequests(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/friends/requests/sent") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.sentRequests(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/friends/feed") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.feed(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        post("/friends/block/{userId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.blockUser(call.currentUserId(), call.pathInt("userId")))
        }
        delete("/friends/block/{userId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.unblockUser(call.currentUserId(), call.pathInt("userId")))
        }
        delete("/friends/{userId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.removeFriend(call.currentUserId(), call.pathInt("userId")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
