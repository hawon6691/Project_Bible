package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AnalyticsController(
    private val service: AnalyticsService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
