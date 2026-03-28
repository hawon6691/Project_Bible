package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class BadgeController(
    private val service: BadgeService,
) {
    fun Route.register() {
        get("/badges") {
            call.respondStub(service.listBadges())
        }
        get("/badges/me") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.myBadges(call.pbActor().userId ?: 0))
        }
        get("/users/{id}/badges") {
            call.respondStub(service.userBadges(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        post("/admin/badges") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/admin/badges/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.parameters["id"]?.toIntOrNull() ?: 0, call.receive()))
        }
        delete("/admin/badges/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        post("/admin/badges/{id}/grant") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.grant(call.parameters["id"]?.toIntOrNull() ?: 0, call.receive(), call.pbActor().userId))
        }
        delete("/admin/badges/{id}/revoke/{userId}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.revoke(
                    badgeId = call.parameters["id"]?.toIntOrNull() ?: 0,
                    userId = call.parameters["userId"]?.toIntOrNull() ?: 0,
                ),
            )
        }
    }
}
