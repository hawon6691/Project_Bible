package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class SpecController(
    private val service: SpecService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
