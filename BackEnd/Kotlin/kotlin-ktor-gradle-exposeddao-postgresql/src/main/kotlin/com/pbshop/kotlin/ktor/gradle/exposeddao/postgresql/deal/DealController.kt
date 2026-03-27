package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

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

class DealController(
    private val service: DealService,
) {
    fun Route.register() {
        get("/deals") {
            call.respondStub(
                service.list(
                    type = call.request.queryParameters["type"],
                    page = call.request.queryParameters["page"]?.toIntOrNull(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
        get("/deals/{id}") {
            call.respondStub(service.detail(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        post("/deals") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/deals/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.parameters["id"]?.toIntOrNull() ?: 0, call.receive()))
        }
        delete("/deals/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
    }
}
