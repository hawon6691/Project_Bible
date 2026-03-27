package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

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

class CrawlerController(
    private val service: CrawlerService,
) {
    fun Route.register() {
        get("/crawler/admin/jobs") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.jobs(
                    status = call.request.queryParameters["status"],
                    page = call.request.queryParameters["page"]?.toIntOrNull(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
        post("/crawler/admin/jobs") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/crawler/admin/jobs/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.parameters["id"]?.toIntOrNull() ?: 0, call.receive()))
        }
        delete("/crawler/admin/jobs/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        post("/crawler/admin/jobs/{id}/run") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.run(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        post("/crawler/admin/triggers") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.trigger(call.receive()))
        }
        get("/crawler/admin/runs") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.runs(
                    status = call.request.queryParameters["status"],
                    jobId = call.request.queryParameters["jobId"]?.toIntOrNull(),
                    page = call.request.queryParameters["page"]?.toIntOrNull(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
        get("/crawler/admin/monitoring") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.monitoring())
        }
    }
}
