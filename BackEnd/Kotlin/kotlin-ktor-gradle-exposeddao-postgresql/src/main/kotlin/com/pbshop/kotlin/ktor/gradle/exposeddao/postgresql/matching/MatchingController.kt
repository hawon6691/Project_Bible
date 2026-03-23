package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class MatchingController(
    private val service: MatchingService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
