package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ShortformController(
    private val service: ShortformService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
