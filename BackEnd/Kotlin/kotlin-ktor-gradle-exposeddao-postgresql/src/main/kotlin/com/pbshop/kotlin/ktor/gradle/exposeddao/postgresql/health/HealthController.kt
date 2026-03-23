package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.health

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondSuccess
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class HealthController(
    private val healthService: HealthService,
) {
    fun Route.registerPublicRoutes() {
        get("/health") {
            val (status, payload) = healthService.response()
            call.respondSuccess(status = status, data = payload)
        }
    }

    fun Route.registerApiRoutes() {
        get("/health") {
            val (status, payload) = healthService.response()
            call.respondSuccess(status = status, data = payload)
        }
    }
}
