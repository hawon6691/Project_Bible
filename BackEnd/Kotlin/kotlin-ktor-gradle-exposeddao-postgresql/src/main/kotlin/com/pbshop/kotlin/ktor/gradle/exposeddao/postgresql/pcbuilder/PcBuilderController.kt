package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

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

class PcBuilderController(
    private val service: PcBuilderService,
) {
    fun Route.register() {
        get("/pc-builds/popular") {
            call.respondStub(
                service.popular(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/pc-builds/shared/{shareCode}") {
            call.respondStub(service.sharedBuild(call.parameters["shareCode"].orEmpty()))
        }
        get("/pc-builds") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.myBuilds(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        post("/pc-builds") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.create(call.currentUserId(), call.receive()))
        }
        get("/pc-builds/{id}/compatibility") {
            call.respondStub(service.compatibility(call.pathInt("id")))
        }
        get("/pc-builds/{id}/share") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.share(call.currentUserId(), call.pathInt("id")))
        }
        post("/pc-builds/{id}/parts") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.addPart(call.currentUserId(), call.pathInt("id"), call.receive()))
        }
        delete("/pc-builds/{id}/parts/{partId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.removePart(call.currentUserId(), call.pathInt("id"), call.pathInt("partId")))
        }
        get("/pc-builds/{id}") {
            call.respondStub(service.detail(call.pathInt("id")))
        }
        patch("/pc-builds/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.update(call.currentUserId(), call.pathInt("id"), call.receive()))
        }
        delete("/pc-builds/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.delete(call.currentUserId(), call.pathInt("id")))
        }
        get("/admin/compatibility-rules") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.listRules())
        }
        post("/admin/compatibility-rules") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.createRule(call.receive()))
        }
        patch("/admin/compatibility-rules/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateRule(call.pathInt("id"), call.receive()))
        }
        delete("/admin/compatibility-rules/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteRule(call.pathInt("id")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
