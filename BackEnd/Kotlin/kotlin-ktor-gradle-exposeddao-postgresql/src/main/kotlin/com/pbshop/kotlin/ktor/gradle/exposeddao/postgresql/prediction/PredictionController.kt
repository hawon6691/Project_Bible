package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class PredictionController(
    private val service: PredictionService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
