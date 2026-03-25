package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

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

class CommunityController(
    private val service: CommunityService,
) {
    fun Route.register() {
        get("/boards") { call.respondStub(service.boards()) }
        get("/boards/{boardId}/posts") {
            call.respondStub(
                service.posts(
                    boardId = call.boardId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    search = call.request.queryParameters["search"],
                    sort = call.request.queryParameters["sort"],
                ),
            )
        }
        get("/posts/{id}") { call.respondStub(service.detail(call.postId())) }
        post("/boards/{boardId}/posts") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createPost(call.currentUserId(), call.boardId(), call.receive()))
        }
        patch("/posts/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val role = call.currentRole()
            call.respondStub(service.updatePost(if (role == PbRole.ADMIN) 0 else call.currentUserId(), role == PbRole.ADMIN, call.postId(), call.receive()))
        }
        delete("/posts/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val role = call.currentRole()
            call.respondStub(service.deletePost(if (role == PbRole.ADMIN) null else call.currentUserId(), role == PbRole.ADMIN, call.postId()))
        }
        post("/posts/{id}/like") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.toggleLike(call.currentUserId(), call.postId()))
        }
        get("/posts/{id}/comments") { call.respondStub(service.comments(call.postId())) }
        post("/posts/{id}/comments") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createComment(call.currentUserId(), call.postId(), call.receive()))
        }
        delete("/comments/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val role = call.currentRole()
            call.respondStub(service.deleteComment(if (role == PbRole.ADMIN) null else call.currentUserId(), role == PbRole.ADMIN, call.commentId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.boardId(): Int = parameters["boardId"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.postId(): Int = parameters["id"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.commentId(): Int = parameters["id"]?.toIntOrNull() ?: 0
    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
