package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class ObservabilityController(
    private val service: ObservabilityService,
) {
    fun Route.register() {
        get("/admin/observability/metrics") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.metrics())
        }
        get("/admin/observability/traces") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.traces(
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50,
                    pathContains = call.request.queryParameters["pathContains"],
                ),
            )
        }
        get("/admin/observability/dashboard") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.dashboard())
        }
    }
}
