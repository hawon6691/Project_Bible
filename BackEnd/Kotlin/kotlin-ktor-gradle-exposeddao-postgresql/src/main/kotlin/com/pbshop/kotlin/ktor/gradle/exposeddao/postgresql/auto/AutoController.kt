package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AutoController(
    private val service: AutoService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
