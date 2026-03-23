package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class PaymentController(
    private val service: PaymentService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
