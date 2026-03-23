package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class OrderController(
    private val service: OrderService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
