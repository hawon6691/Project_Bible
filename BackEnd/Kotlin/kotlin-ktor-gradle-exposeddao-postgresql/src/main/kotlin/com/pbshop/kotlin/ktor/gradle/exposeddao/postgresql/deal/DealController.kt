package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class DealController(
    private val service: DealService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
