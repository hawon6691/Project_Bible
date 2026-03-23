package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class BadgeController(
    private val service: BadgeService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
