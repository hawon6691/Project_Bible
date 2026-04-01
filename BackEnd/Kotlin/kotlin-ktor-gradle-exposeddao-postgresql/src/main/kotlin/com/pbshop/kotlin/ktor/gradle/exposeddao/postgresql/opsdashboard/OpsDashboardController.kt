package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class OpsDashboardController(
    private val service: OpsDashboardService,
) {
    fun Route.register() {
        get("/admin/ops-dashboard/summary") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.summary())
        }
    }
}
