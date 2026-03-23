package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.activity

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ActivityController(
    private val service: ActivityService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
