package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class SupportController(
    private val service: SupportService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
