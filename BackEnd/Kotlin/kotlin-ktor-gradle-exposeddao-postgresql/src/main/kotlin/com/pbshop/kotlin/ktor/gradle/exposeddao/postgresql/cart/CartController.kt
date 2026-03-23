package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class CartController(
    private val service: CartService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
