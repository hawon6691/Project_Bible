package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ErrorCodeController(
    private val service: ErrorCodeService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
