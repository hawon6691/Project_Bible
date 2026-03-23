package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class SellerController(
    private val service: SellerService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
