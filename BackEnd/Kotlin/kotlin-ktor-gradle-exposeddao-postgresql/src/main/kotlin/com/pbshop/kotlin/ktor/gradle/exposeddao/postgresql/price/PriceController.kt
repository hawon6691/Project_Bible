package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class PriceController(
    private val service: PriceService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
