package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class UsedMarketController(
    private val service: UsedMarketService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
