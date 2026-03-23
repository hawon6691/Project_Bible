package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class PcBuilderController(
    private val service: PcBuilderService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
