package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class PointController(
    private val service: PointService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
