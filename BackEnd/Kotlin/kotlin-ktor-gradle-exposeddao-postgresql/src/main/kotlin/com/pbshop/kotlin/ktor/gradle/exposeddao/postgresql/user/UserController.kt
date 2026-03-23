package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class UserController(
    private val service: UserService,
) {
    fun Route.register() {
        get("/users/me") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.currentUser(call.toPrincipal()))
        }
        patch("/users/me") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.updateCurrentUser(call.toPrincipal(), call.receive()))
        }
        delete("/users/me") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.deleteCurrentUser(call.toPrincipal()))
        }
        get("/users") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.adminUserList(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    search = call.request.queryParameters["search"],
                    status = call.request.queryParameters["status"],
                    role = call.request.queryParameters["role"],
                ),
            )
        }
        patch("/users/{id}/status") {
            call.requireAnyRole(PbRole.ADMIN)
            val userId = call.parameters["id"]?.toIntOrNull() ?: 0
            call.respondStub(service.updateUserStatus(userId, call.receive()))
        }
        get("/users/{id}/profile") {
            val userId = call.parameters["id"]?.toIntOrNull() ?: 0
            call.respondStub(service.profile(userId))
        }
        patch("/users/me/profile") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.updateMyProfile(call.toPrincipal(), call.receive()))
        }
        post("/users/me/profile-image") {
            call.requireAnyRole(PbRole.USER)
            val multipart = call.receiveMultipart()
            multipart.forEachPart { it.dispose() }
            val imageUrl = call.request.headers["X-Profile-Image-Url"] ?: "https://img.pbshop.dev/profiles/${call.toPrincipal().userIdHeader ?: "me"}.webp"
            call.respondStub(service.updateProfileImage(call.toPrincipal(), imageUrl))
        }
        delete("/users/me/profile-image") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.deleteProfileImage(call.toPrincipal()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.toPrincipal(): UserPrincipal =
        UserPrincipal(
            role = currentRole(),
            userIdHeader = request.headers["X-User-Id"]?.toIntOrNull(),
            bearerToken =
                request.headers["Authorization"]
                    ?.removePrefix("Bearer ")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() },
        )
}
