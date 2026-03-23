package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ObservabilityController(
    private val service: ObservabilityService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
