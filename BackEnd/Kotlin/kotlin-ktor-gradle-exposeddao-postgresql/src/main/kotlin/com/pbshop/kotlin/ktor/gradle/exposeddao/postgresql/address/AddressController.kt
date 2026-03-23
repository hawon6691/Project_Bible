package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AddressController(
    private val service: AddressService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
