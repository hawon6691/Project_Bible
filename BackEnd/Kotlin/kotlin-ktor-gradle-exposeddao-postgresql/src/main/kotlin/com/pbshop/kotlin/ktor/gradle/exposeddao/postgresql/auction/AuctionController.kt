package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AuctionController(
    private val service: AuctionService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
