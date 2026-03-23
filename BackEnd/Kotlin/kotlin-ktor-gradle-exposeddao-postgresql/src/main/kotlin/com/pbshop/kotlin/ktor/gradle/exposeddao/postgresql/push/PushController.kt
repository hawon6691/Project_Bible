package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class PushController(
    private val service: PushService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
