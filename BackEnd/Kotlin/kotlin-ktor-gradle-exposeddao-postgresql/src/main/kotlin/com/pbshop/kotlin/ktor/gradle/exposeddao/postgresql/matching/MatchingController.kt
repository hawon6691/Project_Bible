package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class MatchingController(
    private val service: MatchingService,
) {
    fun Route.register() {
        get("/matching/pending") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.pending(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        patch("/matching/{id}/approve") {
            call.requireAnyRole(PbRole.ADMIN)
            val request = call.receive<ApproveMappingRequest>()
            call.respondStub(service.approve(call.pathInt("id"), request.productId, call.currentUserId()))
        }
        patch("/matching/{id}/reject") {
            call.requireAnyRole(PbRole.ADMIN)
            val request = call.receive<RejectMappingRequest>()
            call.respondStub(service.reject(call.pathInt("id"), request.reason, call.currentUserId()))
        }
        post("/matching/auto-match") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.autoMatch(call.currentUserId()))
        }
        get("/matching/stats") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.stats())
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
