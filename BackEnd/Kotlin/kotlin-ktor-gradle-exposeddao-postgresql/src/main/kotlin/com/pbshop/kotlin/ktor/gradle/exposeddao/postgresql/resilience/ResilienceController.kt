package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ResilienceController(
    private val service: ResilienceService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
