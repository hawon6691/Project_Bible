package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class FraudController(
    private val service: FraudService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
