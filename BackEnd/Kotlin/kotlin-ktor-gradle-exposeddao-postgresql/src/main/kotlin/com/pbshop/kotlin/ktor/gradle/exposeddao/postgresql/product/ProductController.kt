package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ProductController(
    private val service: ProductService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
