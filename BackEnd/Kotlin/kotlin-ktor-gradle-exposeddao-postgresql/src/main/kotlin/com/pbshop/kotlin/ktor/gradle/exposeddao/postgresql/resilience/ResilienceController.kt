package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class ResilienceController(
    private val service: ResilienceService,
) {
    fun Route.register() {
        get("/resilience/circuit-breakers") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.list())
        }
        get("/resilience/circuit-breakers/policies") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.policies())
        }
        get("/resilience/circuit-breakers/{name}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.detail(call.parameters["name"].orEmpty()))
        }
        post("/resilience/circuit-breakers/{name}/reset") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.reset(call.parameters["name"].orEmpty()))
        }
    }
}
