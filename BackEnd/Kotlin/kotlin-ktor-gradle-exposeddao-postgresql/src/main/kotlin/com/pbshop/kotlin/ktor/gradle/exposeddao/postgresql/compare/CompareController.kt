package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class CompareController(
    private val service: CompareService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
